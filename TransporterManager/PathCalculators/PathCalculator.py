from collections import defaultdict
from heapq import *

from PathCalculators.SumChoice import best_capacity_calculator
from Space.PathDescriptors import *
from Transporters.Transporter import TransporterInfo


def dijkstra(edges, f, t): #edges = lista dei link | f = nodo di partenza (from) | t = nodo di arrivo (to)

    g = defaultdict(list) #dizionario dove ad ogni nodo viene fatto corrispondere un nodo raggiungibile da esso e il peso per raggiungerlo

    for link in edges: #l = nodo partenza | r = nodo arrivo | c = weight
        id = link.id
        l = link.start.id
        r = link.exit.id
        c = len(link.components)
        """
        print("------")
        print("ID =", id)
        print("START =", l)
        print("END =", r)
        print("WEIGHT =", c)
        """
        g[l].append((c,r,id)) #salva per ogni nodo i movimenti che può fare verso un altro nodo e il peso di tale movimento
        g[r].append((c,l,id)) #salva anche il collegamento opposto

    #print(g)

    q, seen, mins = [(0,f,(),None)], set(), {f: 0} #q = lista di tuple (peso per il nodo da raggiungere, nodo da raggiungere, nodi precedenti da attraversare per raggiungerlo, link da cui raggiungerlo) | seen = set dei nodi già visistati | mins = dizionario in cui ad ogni nodo si fa corrispondere la distanza minima per raggiungerlo dal nodo di partenza

    while q: #fintanto che è presente qualche elemento in q
        #print(q)
        (cost,v1,path, link) = heappop(q) #prende ed elimina il nodo con il minor costo da q
        #print("cost=",cost,"v1=",v1,"path:",path)
        #print (v1 not in seen)
        if v1 not in seen: #se il nodo preso non è in seen
            seen.add(v1) #viene aggiunto in seen
            #print("seen=",seen)
            tmpPath = (v1, link)
            path += ((v1, link),) #viene aggiunto al path
            #print("path=",path)
            #print(v1 == t)
            if v1 == t: #se il nodo è il punto di arrivo si conclude l'algoritmo
                return (cost, path)

            for c, v2, lk in g.get(v1, ()): #prende tutti i nodi collegati al nodo v1 | c = costo per il nodo raggiungibile | v2 = nodo raggiungibile | lk = id del link
                #print("c=",c,"v2=",v2, "link=",lk)
                #print(v2 in seen)
                if v2 in seen: continue #se v2 è in seen (quindi è già stato visitato) si passa al prossimo nodo collegato a v1
                prev = mins.get(v2, None) #si prende la distanza minima precedente trovata per raggiungere v2 (None se non se fosse trovata una prima)
                next = cost + c #si calcola il nuovo costo trovato per raggiungere 2
                if prev is None or next < prev: #se il costo precedente non esiteva o è superiore al nuovo costo trovato
                    #print("link usato")
                    mins[v2] = next #la distanza minima per v2 viene cambiata nel nuovo costo
                    heappush(q, (next, v2, path, lk)) #viene aggiunto a q la tupla con (costo minimo per raggiungere v2, v2, percroso minimo per raggiungere v2)

    return float("inf")

def dijkstraCross(edges, f, f_in, t, t_out): #edges = lista dei link | f = nodo di partenza (from) | f_in = punto di entrata dell'incorcio di partenza | t = nodo di arrivo (to) | t_out = punto di uscita dall'incrocio di arrivo

    g = defaultdict(list) #dizionario dove ad ogni nodo viene fatto corrispondere un nodo raggiungibile da esso e il peso per raggiungerlo

    for link in edges: #l = nodo partenza | r = nodo arrivo | c = weight
        id = link.id
        l = link.start.id
        l_in = link.start_in
        r = link.exit.id
        r_in = link.exit_in
        c = len(link.components)
        """print("------")
        print("ID =", id)
        print("START =", l, l_in)
        print("END =", r, r_in)
        print("WEIGHT =", c)"""
        g[l].append((l_in,c,r,id)) #salva per ogni nodo i movimenti che può fare verso un altro nodo e il peso di tale movimento
        g[r].append((r_in,c,l,id)) #salva anche il collegamento opposto

    #print(g,"\n\n")

    q, seen, mins = [(0,f,(),None,f_in)], set(), {f+f_in: 0} #q = lista di tuple (peso per il nodo da raggiungere, nodo da raggiungere, nodi precedenti da attraversare per raggiungerlo, link da cui raggiungerlo, punto di ingresso) | seen = set dei nodi già visistati | mins = dizionario in cui ad ogni nodo si fa corrispondere la distanza minima per raggiungerlo dal nodo di partenza

    #print("q =", q)
    while q: #fintanto che è presente qualche elemento in q
        (cost, v1, path, link, entr) = heappop(q) #prende ed elimina il nodo con il minor costo da q
        """print("NUOVO NODO:\ncost=",cost,"| v1=",v1,"| path:",path,"| entr:",entr)
        print("seen=",seen)
        print("((", v1, ",", entr, ") not in seen):", (v1, entr) not in seen)"""
        if (v1,entr) not in seen: #se il nodo preso non è in seen
            seen.add((v1,entr),) #viene aggiunto in seen
            #print("seen=",seen)
            tmpPath = (v1, link, entr)
            path += ((v1, link, entr),) #viene aggiunto al path
            #print("path=",path)
            #print(v1 == t)
            if v1 == t: #se il nodo è il punto di arrivo si conclude l'algoritmo
                return (cost, path)

            for en, c, v2, lk in g.get(v1, ()): #prende tutti i nodi collegati al nodo v1 | c = costo per il nodo raggiungibile | v2 = nodo raggiungibile | lk = id del link
                """print("\n\nen=",en,"| c=",c,"| v2=",v2, "| link=",lk)
                print(en," == ",entr,":",en == entr)"""
                if en == entr: continue #se il link preso in esame parte dal punto di entrata, il nodo viene scartato
                """print("procedo")
                print("((",v2,",",en,") in seen):",(v2,en) in seen)"""
                if (v2,en) in seen: continue #se v2 e il punto di entrata è in seen (quindi è già stato visitato da quel punto) si passa al prossimo nodo collegato a v1
                prev = mins.get(v2+en, None) #si prende la distanza minima precedente trovata per raggiungere v2 (None se non se fosse trovata una prima)
                next = cost + c #si calcola il nuovo costo trovato per raggiungere 2
                if prev is None or next < prev: #se il costo precedente non esiteva o è superiore al nuovo costo trovato
                    #print("link usato")
                    mins[v2+en] = next #la distanza minima per v2 viene cambiata nel nuovo costo
                    heappush(q, (next, v2, path, lk, en)) #viene aggiunto a q la tupla con (costo minimo per raggiungere v2, v2, percroso minimo per raggiungere v2)
                #print("q =",q)

    return float("inf")

def alwaysStraight(sectors, direction, start, goal):
    # i settori vengono inseriti nel verso di percorrenza True
    tmp_sectors = sectors[:]  # si crea una copia della lista dei settori
    if direction == False: #se la direzione del transporter risulta False
        tmp_sectors.reverse() #e si inverte il senso dei settori

    path = []
    for n in range(len(tmp_sectors)):
        if start == tmp_sectors[n].id:
            start_ind = n
            break
    else:
        return False #start non è stato trovato

    tmp_sectors = tmp_sectors[n:] + tmp_sectors[:n] #modifica la lista facendola partire da start

    for n in range(len(tmp_sectors)):
        if goal == tmp_sectors[n].id:
            path.append(tmp_sectors[n])
            break
        else:
            path.append(tmp_sectors[n])
    else:
        return False #goal non trovato

    return [len(path), path]

#OUTPUT: restituisce una lista di transporter
def pathComparator(sectors, transporters, goal):
    for n in range(len(transporters)): #prende ogni set di transporter
        result = []
        combination_info = []
        total_length = 0
        tmp_transp_paths = []
        for m in range(len(transporters[n])): #prende ogni transporter del set
            length, path = alwaysStraight(sectors, transporters[n][m].direction, transporters[n][m].sector, goal) #per ognuno calcola la lunghezza del percorso
            total_length += length
            #tmp_transp_paths.append([transporters[n][m],path])
        combination_info += [transporters[n], total_length] #crea la combination nfo che indica la lunghezza totale del percorso per la combinazione di transporter presa in esame
        if len(result) == 0: #vuol dire che è ancora vuoto e deve essere ancora inserito il primo risultato
            result = combination_info
        else:
            if result[1] > combination_info[1]:
                result = combination_info

    return result[0]

#calcola i percorsi per tutti i trasportatori verso un unico punto
def pathCalculatorSameGoal(sectors, transporters, goal):
    result = []
    for n in range(len(transporters)):
        tmp_tran = transporters[n]
        length, path = alwaysStraight(sectors, tmp_tran.direction, tmp_tran.sector, goal)
        result.append([tmp_tran, path])
    return result






if __name__ == "__main__":

    sector_10 = Sector("10", True)
    sector_11 = Sector("11", True)
    sector_12 = Sector("12", True)
    sector_13 = Sector("13", True)
    sector_14 = Sector("14", True)
    sector_15 = Sector("15", True)
    sector_16 = Sector("16", True)
    sector_17 = Sector("17", True)
    sector_01a = Sector("01_0_180", True)
    sector_01b = Sector("01_90_270", True)
    sector_02a = Sector("02_0_180", True)
    sector_02b = Sector("02_90_270", True)

    sectors = [sector_10, sector_11, sector_01a, sector_12, sector_02b, sector_13, sector_14, sector_15, sector_02a, sector_16, sector_01b, sector_17]

    tr1 = TransporterInfo("aa", None, "12", True, 5)
    tr2 = TransporterInfo("bb", None, "16", True, 5)
    tr3 = TransporterInfo("cc", None, "13", True, 3)
    tr4 = TransporterInfo("dd", None, "14", True, 3)
    tr5 = TransporterInfo("ee", None, "15", True, 1)

    transporters = [tr1, tr2, tr3, tr4, tr5]

    target = 10

    goal = '17'

    best_transporters = best_capacity_calculator(transporters, target)

    print("BEST COMBINATION:")

    for n in range(len(best_transporters)):
        strRes = ""
        for m in range(len(best_transporters[n])):
            strRes += best_transporters[n][m].mac_address + "-" + str(best_transporters[n][m].capacity) + "|"
        print(strRes)

    if len(best_transporters) != 1:  # se non è stata trovata solo una combinazione
        chosen_transporters = pathComparator(sectors, best_transporters, goal)  # si controlla quale combinazione è nel complesso più vicina al transactor
    else:  # si sceglie la combinazione trovata senza ulteriori controlli
        chosen_transporters = pathCalculatorSameGoal(sectors, best_transporters, goal)


    print("TRANSPORTER - PATH:")
    for n in range(len(chosen_transporters)):
        paths = chosen_transporters[n][0].mac_address + " - "
        for m in range(len(chosen_transporters[n][1])):
            paths += chosen_transporters[n][1][m].id + "|"
        print(paths)



