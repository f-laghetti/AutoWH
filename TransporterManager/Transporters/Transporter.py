class TransporterInfo:

    def __init__ (self, mac, transporter, sector, direction, capacity): #per i transporter su pecorso loop
        self.mac_address = mac
        self.controller = transporter
        self.sector = sector
        self.rest = sector
        self.status = "UNUSED"
        self.direction = direction
        self.capacity = int(capacity)

    def toString(self):
        return "MAC Address:"+ self.mac_address + " | Status:"+ self.status + " | Position:"+ self.sector + " | Direction:"+ self.direction + " | Capacity:" + str(self.capacity)