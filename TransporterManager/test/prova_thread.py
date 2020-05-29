from Space.PathDescriptors import Sector
from threading import Thread

from Transporters.Transporter import TransporterInfo
from Transporters.overdrive import Overdrive

from time import sleep

sector_10 = Sector("10", False)
sector_11 = Sector("11", False)
sector_12 = Sector("12", False)
sector_13 = Sector("13", False)
sector_14a = Sector("14a", False)
sector_14b = Sector("14b", False)
sector_15 = Sector("15", False)
sector_16 = Sector("16", False)
sector_17 = Sector("17", False)
sector_18 = Sector("18", False)
sector_19 = Sector("19", False)
sector_20 = Sector("20", False)
sector_21 = Sector("21", False)
sector_22 = Sector("22", False)
sector_23 = Sector("23", False)
sector_01a = Sector("01_0_180", True, "01")
sector_01b = Sector("01_90_270", True, "01")
sector_02a = Sector("02_0_180", True, "02")
sector_02b = Sector("02_90_270", True, "02")

sectors = [sector_10, sector_11, sector_01a, sector_12, sector_13, sector_02b, sector_14a, sector_14b, sector_15, sector_16, sector_17, sector_18, sector_19, sector_20, sector_02a, sector_21, sector_22, sector_01b, sector_23]

crosses = {}

sectors_queue = {}

for n in range(len(sectors)):
   if sectors[n].type:
      crosses[sectors[n].cross] = []
print(crosses)

for n in range(len(sectors)):
   if sectors[n].type == False:
      sectors_queue[sectors[n].id] = []
print(sectors_queue)

transporters = []

def connect_transporter(mac_address, sector, direction, capacity):
   tmp_conn = Overdrive(mac_address)
   if tmp_conn.is_connected():
      transporters.append(TransporterInfo(mac_address, tmp_conn, sector, direction, capacity))
      print("CONNESSO")

class transporterController (Thread):
   def __init__(self, tr, goal, goal2, sectors, order_type): #TODO passare crosses e sector_queue
      Thread.__init__(self)
      self.kill_thread = False
      self.tr = tr
      self.first_goal = goal
      self.first_goal_flag = False
      self.second_goal = goal2
      self.second_goal_flag = False
      self.rest = False
      self.sectors = sectors[:]
      if tr.direction == False:
         self.sectors.reverse()
      for n in range(len(self.sectors)):
         if tr.sector == self.sectors[n].id:
            self.idx = n
            break
      self.order_type = order_type

   def transitionCallback(self, addr):
      startSec = self.sectors[self.idx]
      if startSec.type and len(crosses[startSec.cross]) and crosses[startSec.cross][0] == self.tr.mac_address : #se il settore da dove proviene è un incrocio e la prima prenotazione per quell'incrocio è la propria (serve a capire se il transporter ha appena iniziato a muoversi da un settore adiacente ad un incrocio)
         crosses[startSec.cross].pop(0)  # rimuove il primo elemento della lista relativo all'incrocio appena superato, ovvero la propria prenotazione
      self.idx = (self.idx + 1) % len(self.sectors)
      nextSec = self.sectors[self.idx]
      tmp_idx = (self.idx + 1) % len(self.sectors)
      nextNextSec = self.sectors[tmp_idx]
      print("Transition from " + addr + "| STARTED:" + startSec.id + "| ARRIVED:" + nextSec.id + "| NEXT:" + nextNextSec.id)

      if self.first_goal_flag == False and nextSec.id == self.first_goal:
         print("ARRIVATO NEL PRIMO PUNTO", nextSec.id)
         self.tr.controller.changeSpeed(0,1000)
         if self.order_type == "DELIVER_SHELF":
            self.tr.controller.light("greenwater")
         elif self.order_type == "RETRIEVE_SHELF":
            self.tr.controller.light("lavender")
         sleep(3.0) #simula l'esecuzione del lavoro in 3 secondi
         self.first_goal_flag = True
         self.tr.controller.light("blue")
         self.tr.controller.changeSpeed(500, 500)  # riparte
         self.tr.controller.changeLaneLeft(500,500) #ritorna sulla corsia di camminamento
         sectors_queue[nextSec.id].pop(0)  # rimuove il primo elemento della lista relativo al settore appena superato, ovvero la propria prenotazione

      if self.first_goal_flag == True and self.second_goal_flag == False and nextSec.id == self.second_goal:
         print("ARRIVATO NEL SECONDO PUNTO", nextSec.id)
         self.tr.controller.changeSpeed(0, 1000)
         if self.order_type == "DELIVER_SHELF":
            self.tr.controller.light("yellow")
         elif self.order_type == "RETRIEVE_SHELF":
            self.tr.controller.light("orange")
         sleep(3.0)  # simula l'esecuzione del lavoro in 3 secondi
         self.second_goal_flag = True
         self.tr.controller.light("blue")
         self.tr.controller.changeSpeed(500, 500)  # riparte
         self.tr.controller.changeLaneLeft(500, 500)  # ritorna sulla corsia di camminamento
         sectors_queue[nextSec.id].pop(0)  # rimuove il primo elemento della lista relativo al settore appena superato, ovvero la propria prenotazione
         self.rest = True

      if self.rest == True and nextSec.id == self.tr.rest:
         print("ARRIVATO NEL PUNTO DI RESt", nextSec.id)
         self.tr.controller.changeSpeed(0, 1000)
         self.tr.controller.light("red")
         self.kill_thread = True

      if nextNextSec.type: #prossimo sector è un incrocio
         crosses[nextNextSec.cross].append(self.tr.mac_address) #aggiunge se stesso alla coda per accedere all'incrocio
         print(crosses)
         if crosses[nextNextSec.cross][0] != self.tr.mac_address: #se il primo elemento della lista è un altro transporter
            self.tr.controller.changeSpeed(0, 1000) #si ferma
            wait = True
            while wait:
               if crosses[nextNextSec.cross][0] != self.tr.mac_address:
                  sleep(0.5)
               else:
                  self.tr.controller.changeSpeed(500, 500) #riparte
                  self.tr.controller.light("blue")
                  wait = False

      if (self.first_goal_flag == False and nextNextSec.id == self.first_goal) or (self.first_goal_flag == True and self.second_goal_flag == False and nextNextSec.id == self.second_goal): #prossimo sector è un goal
         self.tr.controller.changeLaneRight(500,500)
         sectors_queue[nextNextSec.id].append(self.tr.mac_address)
         print(sectors_queue)
         if sectors_queue[nextNextSec.id][0] != self.tr.mac_address:  # se il primo elemento della lista è un altro transporter
            sleep(0.2) #il tempo di sistemarsi nella corsia laterale
            self.tr.controller.changeSpeed(0, 1000)  # si ferma
            self.tr.controller.light("pink")
            wait = True
            while wait:
               if sectors_queue[nextNextSec.id][0] != self.tr.mac_address:
                  sleep(0.5)
               else:
                  self.tr.controller.changeSpeed(500, 500)  # riparte
                  wait = False

      if self.rest == True and nextNextSec.id == self.tr.rest: #se il prossimo sector è il punto di rest e il transporter deve tornare in rest
         self.tr.controller.changeLaneRight(500, 500)


   def locationChangeCallback(self, addr, location, piece, speed, clockwise, offset):
      self.tr.controller.offset = offset
      print(addr," - ",offset)

   def run(self):
      self.tr.controller.changeSpeed(250,500)
      self.tr.controller.changeLaneLeft(500,500)
      #self.tr.controller.setLocationChangeCallback(self.locationChangeCallback)
      self.tr.controller.setTransitionCallback(self.transitionCallback)
      while self.kill_thread == False:
         pass
      print("Thread Chiuso")
      """while self.tr.controller.offset == 0:
         wait=True
      self.tr.controller.changeLane(500,500,25)"""


if __name__ == "__main__":
   connect_transporter("c0:e2:c0:f8:39:76", '19', True, 3)
   connect_transporter("c3:76:2f:0b:70:18", '22', False, 3)
   connect_transporter("c3:10:2a:28:10:45", '15', False, 3)

   thread1 = transporterController(transporters[0], '00', '15', sectors, "DELIVER_SHELF")
   thread2 = transporterController(transporters[1], '00', '15', sectors, "DELIVER_SHELF")
   thread3 = transporterController(transporters[2], '00', '13', sectors, "RETRIEVE_SHELF")

   thread1.start()
   thread2.start()
   thread3.start()

