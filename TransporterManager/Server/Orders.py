class Order:

    def __init__(self, id, type, transactor, tot_wares, shelves, command):
        self.id = id
        self.type = type
        self.transactor = transactor
        self.tot_wares = tot_wares
        self.shelves = shelves
        self.command = command
        self.in_progress = False #flag che controlla se l'ordine sta venendo eseguito