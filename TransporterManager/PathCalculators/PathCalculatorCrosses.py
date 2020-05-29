from collections import defaultdict
from heapq import *

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

if __name__ == "__main__":
    edges = [
        ("1", "2", 1),
        ("1", "2", 1),
        ("1", "3", 8),
        ("1", "6", 7),
        ("2", "3", 2),
        ("2", "6", 5),
        ("3", "4", 2),
        ("3", "5", 2),
        ("4", "4", 3),
        ("4", "5", 1),
        ("5", "6", 1),
        ("5", "6", 3)
    ]

    print("=== Dijkstra ===")
    print (edges)

    print ("1 -> 6:")
    weight, path = dijkstra(edges, "1", "6")
    print ("WEIGHT : "+str(weight))
    print ("PATH : "+str(path))

    print ("5 -> 2:")
    weight, path = dijkstra(edges, "5", "2")
    print("WEIGHT : " + str(weight))
    print("PATH : " + str(path))

    print ("4 -> 6:")
    weight, path = dijkstra(edges, "4", "6")
    print("WEIGHT : " + str(weight))
    print("PATH : " + str(path))

    print ("3 -> 4:")
    weight, path = dijkstra(edges, "3", "4")
    print("WEIGHT : " + str(weight))
    print("PATH : " + str(path))