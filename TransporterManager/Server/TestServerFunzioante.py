from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread

from PathCalculators.PathCalculator import pathComparator, pathCalculatorSameGoal
from PathCalculators.SumChoice import best_capacity_calculator, compare_total_capacity
from Server import AutoMagParsers
from Server.Orders import Order
from Transporters.Transporter import TransporterInfo
from Transporters.overdrive import *
from Space.PathDescriptors import *

from time import sleep

class AutoMagHTTPRequestHandler(BaseHTTPRequestHandler):

    order_in_progress = False #flag che indica se un ordine è in corso

    order_queue = [] #lista degli ordini da completare (il pimo è quello in corso)

    transporters = []

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

    crosses_queue = {}

    sectors_queue = {}

    for n in range(len(sectors)):
        if sectors[n].type:
            crosses_queue[sectors[n].cross] = []

    for n in range(len(sectors)):
        if sectors[n].type == False:
            sectors_queue[sectors[n].id] = []

    def _set_headers(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()

    def set_response(self, message):
        """This just generates an HTML document that includes `message`
        in the body. Override, or re-write this do do more interesting stuff.
        """
        content = f"<html><body><h1>{message}</h1></body></html>"
        return content.encode("utf8")  # NOTE: must return a bytes object!

    def do_POST(self):
        # Doesn't do anything with posted data
        content_length = int(self.headers['Content-Length']) # <--- Gets the size of data
        post_data = self.rfile.read(content_length) # <--- Gets the data itself
        parsed_command = AutoMagParsers.parse_POST_command(str(post_data))
        print(parsed_command)
        self.execute_command(parsed_command)
        self._set_headers()
        self.wfile.write(self.set_response(post_data))

    #metodo che chiama il metodo corrispondente al comando
    #IPOTESI: ogni comando crea un thread
    def execute_command(self, command):
        if command[0] == "CONNECT":
            print("Transporters connection requested")
            connected = self.connect_transporter(command[1], command[2], command[3], command[4])
            if connected == True:
                print("Transporters", command[1], "connected" )
            else:
                print("Transporters", command[1], "failed to connect" )
            self.print_transporters()
            return connected
        elif command[0] == "RETRIEVE_SHELF":
            print("Request to send a Transporters to retrieve item from some shelves")
            self.order_queue.append(Order(command[1], command[0], command[2], command[3], command[4]))
            #self.retrieve_from_shelf(command)
            #calcolo il percorso più breve e il trasportatore da usare
            #creo thread che gestisce il movimento (se è possibile fare la richiesta)
            #invio risposta al sistema
        elif command[0] == "DELIVER_SHELF":
            print("Request to send a Transporters to deliver items to some shelves")
            self.order_queue.append(Order(command[1], command[0], command[2], command[3], command[4]))
            print("order_queue:",self.order_queue)

            #self.deliver_to_shelf(command, self.order_in_progress)
            deliverThread = deliverShelfExecutor(self,command)
            deliverThread.start()
            #cerco i trasportatori liberi
            #se la quantità da prendere è maggiore della somma totale della capacità dei trasortatori disponibili, li mando tutti
            #sennò capisco qual è la combinazione migliore di trasportatori (minima capacità complessiva e minor tempo di arrivo al transactor)
            #invio i transporter
        elif command[0] == "TO_CHARGE":
            print("Request to send a Transporters to a charge station")
        elif command[0] == "TO_REST":
            print("Request to send a Transporters to its rest point")
        else:
            print("ERROR: unknown command")

    #metodo che controlla se il transporter è presente nella lista "transporters"
    def check_transporter(self, mac_address):
        for n in range(len(self.transporters)): #controlla tutti i transporter gia connessi
            if mac_address == self.transporters[n].mac_address: #se esiste già un transporter con lo stesso mac address non procede
                return True
        return False

    # metodo che restituisce la lista di tutti i transporters liberi
    def check_unused_transporter(self):
        unused_list = []
        for n in range(len(self.transporters)):  # controlla tutti i transporter gia connessi
            if self.transporters[n].status == "UNUSED":
                unused_list.append(self.transporters[n])
        return unused_list

    #metodo per connettersi ad un transporter e salvarlo tra i transporter connessi
    def connect_transporter(self, mac_address, sector, direction, capacity):
        if self.check_transporter(mac_address): #controlla che il transporter non sia già presente nella lista "transporters"
            return False #se dovesse risultare già presente, la nuova connessione non viene eseguita
        #se il mac address non è già nella lista dei transporter ci si connette al transporter e si salvano le sue info nella lista "transporters"
        tmp_conn = Overdrive(mac_address)
        if tmp_conn.is_connected():
            self.transporters.append(TransporterInfo (mac_address, tmp_conn, sector, direction, capacity))
            tmp_conn.light("red")
            return True
        return False

    #metodo per inviare un transporter a prelevare le merci dalle scaffalature designate
    def retrieve_from_shelf(self, command):
        mac_address = command[1]
        if self.check_transporter(mac_address):
            shelves = command[2]
            print("\nTransporters", mac_address, "has been sent to:\n")
            for n in range(len(shelves)):
                tmp_shelf = shelves[n]
                print(n,") Shelf in sector",tmp_shelf[0],"oriz offset",tmp_shelf[1],"vert offset",tmp_shelf[2])
                print("\tLevels from where to retrieve wares", ','.join(tmp_shelf[3]),"\n")
            return True
        else:
            print("Transporters",mac_address,"not found")
            return False

    #metodo per inviare un transporter a consegnare le merci nelle scaffalature designate
    def deliver_to_shelf(self, command, order_in_progress):
        #print("COMANDO DELIVER_SHELF:", command)
        transactor = command[2] #coordinate del transactor
        num_wares = int(command[3]) #numero di merci da prelevare dal transactor
        shelves = command[4]

        # cerco i trasportatori liberi
        unused_transporters = self.check_unused_transporter()

        # se la quantità da prendere è maggiore della somma totale della capacità dei trasortatori disponibili, li mando tutti
        if compare_total_capacity(unused_transporters, num_wares):
            chosen_transporters = pathCalculatorSameGoal(self.sectors, unused_transporters, transactor)
            print("capacità massima inferiore a quella richiesta: invio tutti i trasportatori")
            order_in_progess = True #comunica tramite il flag che l'ordine non è ncora stato completato
        else:
            # sennò capisco qual è la combinazione migliore di trasportatori (minima capacità complessiva e minor tempo di arrivo al transactor)
            best_transporters = best_capacity_calculator(unused_transporters, num_wares)
            if len(best_transporters) != 1: #se non è stata trovata solo una combinazione
                chosen_transporters = pathComparator(self.sectors, best_transporters, transactor) #si controlla quale combinazione è nel complesso più vicina al transactor
            else: #si sceglie la combinazione trovata senza ulteriori controlli
                chosen_transporters = pathCalculatorSameGoal(self.sectors, best_transporters[0], transactor)
            order_in_progess = False #comunica tramite il flag che l'ordine sta venendo completato


        # invio i transporter

        return True

    #setta lo status di tutti i transporter dati con lo status desiderato (WORKING di default)
    def setTransportersStatus(self, transporters, status = "WORKING"):
        for n in range(len(transporters)):
            transporters[n].status = status

    #PRINT METHODS
    def print_transporters(self):
        print("\n|--------------------------|")
        print("TRANSPORTERS LIST:")
        for n in range(len(self.transporters)):
            print(n+1,")",self.transporters[n].toString(),"")
        print("|--------------------------|")

class deliverShelfExecutor (Thread): #thread che si occupa di gestire un ordine di deliver_shelf
    #handler: l'istanza che ha creato questo thread
    #command: il comando
    def __init__(self, handler, command):
        Thread.__init__(self)
        print("Thread DeliverShelfExecutor initialized")
        self.handler = handler

        self.order_id = command[1] #id dell'ordine
        self.transactor = command[2]  # coordinate del transactor
        self.num_wares = int(command[3])  # numero di merci da prelevare dal transactor
        self.remaining_shelves = command[4] #lista delle shelves rimanenti

        self.retrieve_transanctor = True #flag che indica se bisogna ancora prendere merce dal transactor

        self.deliver_shelf = True  # flag che indica se bisogna ancora portare merce alle scaffalature

        # print("COMANDO DELIVER_SHELF:", command)

    def send_to_transactor(self):
        # fintanto che ci sono ancora merci da prendere nel transactor
        while self.retrieve_transanctor:
            #svuoto la lista dei transporter unused
            unused_transporters = []
            # cerco i trasportatori liberi
            unused_transporters = self.handler.check_unused_transporter()

            if len(unused_transporters) != 0: #se è stato trovato almeno un transporter unused

                print("UNUSED TRANSPORTERS:")
                for n in range(len(unused_transporters)):
                    print(unused_transporters[n].mac_address)

                # se la quantità da prendere è maggiore della somma totale della capacità dei trasortatori disponibili, li mando tutti
                print("self.num_wares prima =",self.num_wares)
                if compare_total_capacity(unused_transporters, self.num_wares):
                    # vengono selezionati tutti i transporter
                    chosen_transporters = unused_transporters
                    print("capacità massima inferiore a quella richiesta: invio tutti i trasportatori")
                else:

                    print("self.num_wares dopo =", self.num_wares)
                    # sennò capisco qual è la combinazione migliore di trasportatori (minima capacità complessiva e minor tempo di arrivo al transactor)
                    best_transporters = best_capacity_calculator(unused_transporters, self.num_wares)

                    print("BEST TRANSPORTERS:")
                    for n in range(len(best_transporters)):
                        print("LISTA #",n+1)
                        for m in range(len(best_transporters[n])):
                            print(best_transporters[n][m].mac_address)

                    if len(best_transporters) != 1:  # se non è stata trovata solo una combinazione
                        chosen_transporters = pathComparator(self.handler.sectors, best_transporters, self.transactor)  # si controlla quale combinazione è nel complesso più vicina al transactor
                    else:  # si sceglie la combinazione trovata senza ulteriori controlli
                        chosen_transporters = best_transporters[0] #TODO vedi qua
                    self.retrieve_transanctor = False  # comunica tramite il flag che sono stati inviati abbastanza transporter da prelevare tutte le merci dal transactor
                #setto i trasportatori scelti su working
                self.handler.setTransportersStatus(chosen_transporters)

                print("CHOSEN TRANSPORTERS:")
                for n in range(len(chosen_transporters)):
                    print(chosen_transporters[n].mac_address)

                self.handler.print_transporters()
                #assegno ad ogni transporter le shelf da consegnare
                assigned_transporters = self.assignShelves(chosen_transporters, self.remaining_shelves)

                print("ASSIGNED TRANSPORTERS:")
                for n in range(len(assigned_transporters)):
                    print(assigned_transporters[n][0].mac_address)

                #invio i transporter
                for n in range(len(assigned_transporters)): #cicla tra i transporter scelti
                    tmp_assigned = assigned_transporters[n][0] #transporter assegnato
                    tmp_diz_shelf = assigned_transporters[n][1] #dizionario delle shelf del transporter
                    #si calcola quante wares totali dovrà trasportare il trasportatore
                    tot_delivered = 0
                    for m in range(len(tmp_diz_shelf)): #cicla tra tutte le shelf assegnate al trasportatore
                        tot_delivered += len(tmp_diz_shelf[list(tmp_diz_shelf)[m]])
                    tmp_diz_transactor = {}
                    tmp_diz_transactor[self.transactor] = [tot_delivered,]

                    #si invia il transporter
                    tmp_deliver_shelf_controller = transporterControllerDeliverShelf(tmp_assigned,tmp_diz_shelf,tmp_diz_transactor,self.handler.sectors,"DELIVER_SHELF",self.handler.crosses_queue,self.handler.sectors_queue)
                    tmp_deliver_shelf_controller.start()

                #TODO aggiornare il flag che indica che è stata presa tutta la merce


    #OUTPUT: restituisci una lista di liste, ogni lista è formata da un transporter e un dizionario,il dizionario ha come key tutti le shelf assegnate al transporter e come value i livelli di quella shelf
    def assignShelves(self, transporters, shelves):
        result = []  # lista dove ogni elemento è composto dal transporter e un dizionario dove ad ogni shelf corrispondono i livelli da riempire
        shelves_ended = False  # flag che segnala che ci sono ancora livelli delle shelves da assegnare
        for n in range(len(transporters)):  # cicla tutti i transporter
            if shelves_ended:  # se le shelf sono state tutte assegnate
                break
            tmp_tran = transporters[n]  # prende il transporter
            dict_shelves = {}  # dizionario dove ad ogni shelf si fanno corrispondere i livelli da riempire
            remaining_capacity = tmp_tran.capacity  # prende la capacità del transporter
            for m in range(len(shelves)):  # cicla tra le shelves
                if len(shelves[m].levels) == 0:  # se la shelf non ha livelli da assegnare
                    continue  # si passa alal prossima shelf
                if len(shelves[m].levels) <= remaining_capacity:  # se i livelli da riempire della shelf presa in esame sono inferiori o uguali alla capacità del transporter
                    dict_shelves[shelves[m].coord] = shelves[m].levels  # vengono assegnate tutti i livelli della shelf al transporter
                    remaining_capacity -= len(shelves[m].levels)  # si scala la capacità rimanente del transporter
                    shelves[m].levels = []  # vengono rimossi tutti i livelli presi dalla lista delle shelves
                    if m == len(shelves) - 1:  # se fosse l'ultima shelf
                        shelves_ended = True  # vuol dire che tutte le shelf sono state assegnate
                else:  # se i livelli da riempire della shelf presa in esame sono superiori alla capacità del transporter
                    dict_shelves[shelves[m].coord] = shelves[m].levels[
                                                     :remaining_capacity]  # viene assegnato il numero di livelli della shelf al transporter uguale alla capacità rimanente
                    shelves[m].levels = shelves[m].levels[
                                        remaining_capacity:]  # vengono rimossi i livelli assegnati dalla lista delle shelf
                    remaining_capacity = 0  # il transporter è totalmente pieno
                if remaining_capacity == 0 or shelves_ended:  # se il transporter non ha più capacità rimamente o le shelves sono state tutte assegnate
                    result.append([transporters[n], dict_shelves])  # si aggiunge cosa si è troavato al risultato
                    break  # si passa al prossimo trasnporter
        return result





    def run(self):
        print("Thread DeliverShelfExecutor started")
        #while self.num_wares != 0: #fintanto che ci sono ancora merci da prendere nel transactor
        self.send_to_transactor()
        print("RECUPERATE TUTTE LE MERCI DAL TRANSACTOR")

        while len(self.remaining_shelves) != 0:#fintanto che ci sono ancora scaffalature da riempire
            pass
        print("ORDINE COMPLETATO")

        print("CHIUSURA THREAD")

class transporterControllerDeliverShelf (Thread):
   def __init__(self, tr, goal, goal2, sectors, order_type, crosses, sectors_queue): #TODO passare crosses e sector_queue
      Thread.__init__(self)
      self.kill_thread = False
      self.tr = tr
      #si dividono i goal in first e second perchè ogni ordine avrà due tipi diferenti di goal: transactor e shelf
      self.first_goal = goal #dizionario dei first_goal, ad ogni key corrispondono i livelli da riempire
      self.first_goal_flag = 0 #indica il prossimo first_goal da completare; quando sarà uguale alla lunghezza di first_goal vorrà dire che tutti i first_goal sono stati raggiunti
      self.second_goal = goal2 #dizionario dei second_goal, ad ogni key corrisponde il numero di pezzi da rilasciare nel transactor
      self.second_goal_flag = 0 #indica il prossimo second_goal da completare
      self.rest = False
      self.sectors = sectors[:]
      if tr.direction == False:
         self.sectors.reverse()
      for n in range(len(self.sectors)):
         if tr.sector == self.sectors[n].id:
            self.idx = n
            break
      self.order_type = order_type
      self.crosses = crosses
      self.sectors_queue = sectors_queue

   def transitionCallback(self, addr):
      startSec = self.sectors[self.idx]
      if startSec.type and len(self.crosses[startSec.cross]) and self.crosses[startSec.cross][0] == self.tr.mac_address : #se il settore da dove proviene è un incrocio e la prima prenotazione per quell'incrocio è la propria (serve a capire se il transporter ha appena iniziato a muoversi da un settore adiacente ad un incrocio)
         self.crosses[startSec.cross].pop(0)  # rimuove il primo elemento della lista relativo all'incrocio appena superato, ovvero la propria prenotazione
      self.idx = (self.idx + 1) % len(self.sectors)
      nextSec = self.sectors[self.idx]
      tmp_idx = (self.idx + 1) % len(self.sectors)
      nextNextSec = self.sectors[tmp_idx]
      #print("Transition from " + addr + "| STARTED:" + startSec.id + "| ARRIVED:" + nextSec.id + "| NEXT:" + nextNextSec.id)

      if self.first_goal_flag != len(self.first_goal) and nextSec.id == list(self.first_goal)[self.first_goal_flag]:
         print("ARRIVATO NEL " + str(self.first_goal_flag+1) + "o FIRST GOAL", nextSec.id)
         print("RIEMPITO LA SCAFFALATURA", list(self.first_goal)[self.first_goal_flag], "NEI LIVELLI",self.first_goal[list(self.first_goal)[self.first_goal_flag]])
         self.tr.controller.changeSpeed(0,1000)
         if self.order_type == "DELIVER_SHELF":
            self.tr.controller.light("greenwater")
         elif self.order_type == "RETRIEVE_SHELF":
            self.tr.controller.light("lavender")
         sleep(3.0) #simula l'esecuzione del lavoro in 3 secondi
         self.first_goal_flag += 1 #aumenta il flag che indica a quale first_goal i è arrivati
         self.tr.controller.light("blue")
         self.tr.controller.changeSpeed(500, 500)  # riparte
         self.tr.controller.changeLaneLeft(500,500) #ritorna sulla corsia di camminamento
         self.sectors_queue[nextSec.id].pop(0)  # rimuove il primo elemento della lista relativo al settore appena superato, ovvero la propria prenotazione

      if self.first_goal_flag == len(self.first_goal) and self.second_goal_flag != len(self.second_goal) and nextSec.id == list(self.second_goal)[self.second_goal_flag]:
         print("ARRIVATO NEL " + str(self.second_goal_flag+1) + "o SECOND GOAL", nextSec.id)
         print("SCARICATO IN", list(self.second_goal)[self.second_goal_flag], "UN AMMONTARE DI", self.second_goal[list(self.second_goal)[self.second_goal_flag]], "PEZZI")
         self.tr.controller.changeSpeed(0, 1000)
         if self.order_type == "DELIVER_SHELF":
            self.tr.controller.light("yellow")
         elif self.order_type == "RETRIEVE_SHELF":
            self.tr.controller.light("orange")
         sleep(3.0)  # simula l'esecuzione del lavoro in 3 secondi
         self.second_goal_flag += 1
         self.tr.controller.light("blue")
         self.tr.controller.changeSpeed(500, 500)  # riparte
         self.tr.controller.changeLaneLeft(500, 500)  # ritorna sulla corsia di camminamento
         self.sectors_queue[nextSec.id].pop(0)  # rimuove il primo elemento della lista relativo al settore appena superato, ovvero la propria prenotazione
         self.rest = True

      if self.rest == True and nextSec.id == self.tr.rest:
         print("ARRIVATO NEL PUNTO DI REST", nextSec.id)
         self.tr.controller.changeSpeed(0, 1000)
         self.tr.controller.light("red")
         self.tr.status = "UNUSED"
         self.kill_thread = True

      if nextNextSec.type and not (self.rest == True and nextSec.id == self.tr.rest): #prossimo sector è un incrocio ma il transactor non si è fermato per il rest
         self.crosses[nextNextSec.cross].append(self.tr.mac_address) #aggiunge se stesso alla coda per accedere all'incrocio
         #print(self.crosses)
         if self.crosses[nextNextSec.cross][0] != self.tr.mac_address: #se il primo elemento della lista è un altro transporter
            self.tr.controller.changeSpeed(0, 1000) #si ferma
            wait = True
            while wait:
               if self.crosses[nextNextSec.cross][0] != self.tr.mac_address:
                  sleep(0.5)
               else:
                  self.tr.controller.changeSpeed(500, 500) #riparte
                  self.tr.controller.light("blue")
                  wait = False

      if (self.first_goal_flag != len(self.first_goal) and nextNextSec.id == list(self.first_goal)[self.first_goal_flag]) or (self.first_goal_flag == len(self.first_goal) and self.second_goal_flag != len(self.second_goal) and nextNextSec.id == list(self.second_goal)[self.second_goal_flag]): #prossimo sector è un goal
         self.tr.controller.changeLaneRight(500,500)
         self.sectors_queue[nextNextSec.id].append(self.tr.mac_address)
         #print(self.sectors_queue)
         if self.sectors_queue[nextNextSec.id][0] != self.tr.mac_address:  # se il primo elemento della lista è un altro transporter
            sleep(0.2) #il tempo di sistemarsi nella corsia laterale
            self.tr.controller.changeSpeed(0, 1000)  # si ferma
            self.tr.controller.light("pink")
            wait = True
            while wait:
               if self.sectors_queue[nextNextSec.id][0] != self.tr.mac_address:
                  sleep(0.5)
               else:
                  self.tr.controller.changeSpeed(500, 500)  # riparte
                  wait = False

      if self.rest == True and nextNextSec.id == self.tr.rest: #se il prossimo sector è il punto di rest e il transporter deve tornare in rest
         self.tr.controller.changeLaneRight(500, 500)

   def run(self):
      #controlla che il settore subito successivo a quello di partenza non sia un incrocio
      tmp_idx = (self.idx + 1) % len(self.sectors)
      nextNextSec = self.sectors[tmp_idx]
      if nextNextSec.type: #se è un incrocio
          self.crosses[nextNextSec.cross].append(self.tr.mac_address)  # aggiunge se stesso alla coda per accedere all'incrocio
          wait = True
          while wait:
              if self.crosses[nextNextSec.cross][0] != self.tr.mac_address: #se non è lui il primo della coda d'accesso all'incocio rimane fermo
                  sleep(0.5)
              else:
                  self.tr.controller.changeSpeed(500, 500)  # riparte
                  self.tr.controller.light("blue")
                  wait = False
      self.tr.controller.light("blue")
      self.tr.controller.changeSpeed(500,500)
      self.tr.controller.changeLaneLeft(500,500)
      #self.tr.controller.setLocationChangeCallback(self.locationChangeCallback)
      self.tr.controller.setTransitionCallback(self.transitionCallback)
      while self.kill_thread == False:
         pass
      print("Thread Chiuso")
      """while self.tr.controller.offset == 0:
         wait=True
      self.tr.controller.changeLane(500,500,25)"""

def serverRun(server_class=HTTPServer, handler_class=AutoMagHTTPRequestHandler, addr="localhost", port=8000):
    server_address = (addr, port)
    httpd = server_class(server_address, handler_class)

    print(f"Starting http server on {addr}:{port}")
    httpd.serve_forever()


if __name__ == "__main__":
    serverRun()