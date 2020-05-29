from Transporters.Transporter import TransporterInfo
from Space.Shelves import Shelf

def assignShelves(transporters, shelves):
    result = []  # lista dove ogni elemento è composto dal transporter e un dizionario dove ad ogni shelf corrispondono i livelli da riempire
    shelves_ended = False  # flag che segnala che ci sono ancora livelli delle shelves da assegnare
    for n in range(len(transporters)):  # cicla tutti i transporter
        if shelves_ended:  # se le shelf sono state tutte assegnate
            break
        tmp_tran = transporters[n]  # prende il transporter
        print("ANALIZZO:",tmp_tran.mac_address)
        dict_shelves = {}  # dizionario dove ad ogni shelf si fanno corrispondere i livelli da riempire
        remaining_capacity = tmp_tran.capacity  # prende la capacità del transporter
        print("-Capacity:",tmp_tran.capacity)
        for m in range(len(shelves)):  # cicla tra le shelves
            print("-Shelf:",shelves[m].coord,"- Levels:",shelves[m].levels)
            print("--Remaining capacity:", remaining_capacity)
            if len(shelves[m].levels) == 0: #se la shelf non ha livelli da assegnare
                continue #si passa alal prossima shelf
            if len(shelves[m].levels) <= remaining_capacity:  # se i livelli da riempire della shelf presa in esame sono inferiori o uguali alla capacità del transporter
                dict_shelves[shelves[m].coord] = shelves[m].levels  # vengono assegnate tutti i livelli della shelf al transporter
                remaining_capacity -= len(shelves[m].levels)  # si scala la capacità rimanente del transporter
                shelves[m].levels = []  # vengono rimossi tutti i livelli presi dalla lista delle shelves
                if m == len(shelves) - 1:  # se fosse l'ultima shelf
                    shelves_ended = True  # vuol dire che tutte le shelf sono state assegnate
            else:  # se i livelli da riempire della shelf presa in esame sono superiori alla capacità del transporter
                dict_shelves[shelves[m].coord] = shelves[m].levels[:remaining_capacity]  # viene assegnato il numero di livelli della shelf al transporter uguale alla capacità rimanente
                shelves[m].levels = shelves[m].levels[remaining_capacity:]  # vengono rimossi i livelli assegnati dalla lista delle shelf
                remaining_capacity = 0  # il transporter è totalmente pieno
            if remaining_capacity == 0 or shelves_ended:  # se il transporter non ha più capacità rimamente o le shelves sono state tutte assegnate
                result.append([transporters[n], dict_shelves]) #si aggiunge cosa si è troavato al risultato
                break # si passa al prossimo trasnporter
    return result

if __name__ == "__main__":

    tr1 = TransporterInfo("aa", None, "12", True, 3)
    tr2 = TransporterInfo("bb", None, "16", True, 3)
    tr3 = TransporterInfo("cc", None, "13", True, 3)

    transporters = [tr1, tr2, tr3]

    sh1 = Shelf("13",[0,1,2,3,4])
    sh2 = Shelf("14", [0,1,2])

    shelves = [sh1, sh2]

    assign = assignShelves(transporters, shelves)

    for n in range(len(shelves)):
        print(shelves[n].coord,"-",shelves[n].levels)

    for n in range(len(assign)):
        print(assign[n][0].mac_address,"-",[assign[n][1]])