from Server.TestServer import *

from threading import Thread

class ServerThread (Thread):
   def __init__(self, addr="localhost", port=8000):
        Thread.__init__(self)
        self.addr = addr
        self.port = port

   def run(self):
        print ("Thread '" + self.name + "' avviato")
        serverRun(port = self.port)
        print ("Thread '" + self.name + "' terminato")

server = ServerThread()

server.start()