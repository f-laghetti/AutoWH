from PathCalculators import *
#from PathCalculators.PathCalculator import dijkstraCross


class Cross: #nodo

    def __init__(self, id):
        self.id = id
        self.adjacent = {}

    def addLink(self, link, exit, cross, cross_in):
        self.adjacent[exit] = [link, cross, cross_in]
        print("ADDED LINK: "+str(self.id)+" "+exit+" <--> "+link.id+" <--> "+str(cross.id)+" "+cross_in)

    def toString(self):
        result = "CROSS "+self.id+"\n\t"
        for c in self.adjacent.keys():
            tmp = self.adjacent[c]
            result += c+" = "+tmp[0].id+" ---> "+tmp[1].id+" "+tmp[2]+"\n\t"
        return result

class Link: #arco

    def __init__(self, id, start, start_in, exit, exit_in, components):
        self.id = id #id del link
        self.start = start #incrocio da cui si origina (considerando il verso orario)
        self.start_in = start_in #punto di entrata nel nodo start
        start.addLink(self, start_in, exit, exit_in) #aggiunge il link nel cross start
        self.exit = exit #incrocio in cui finisce (considerando il verso orario)
        self.exit_in = exit_in  # punto di entrata nel nodo exit
        exit.addLink(self, exit_in, start, start_in) #aggiunge il link nel cross exit
        self.components = components #i singoli componenti che compongono il link
        self.weight = len(components) #numero dei componenti

    def toString(self):
        return "LINK "+self.id+": "+self.start.id+" "+self.start_in+" <---> "+self.exit.id+" "+self.exit_in+" - Weight:"+str(self.weight)

    def componentsToString(self):
        strComp = "| "
        for comp in self.components:
            strComp += comp.id + " | "
        return("COMPONENTS:"+strComp)

class Sector: #singolo settore del percorso, componente di un Link

    def __init__(self, id, type, cross = None):
        self.id = id
        self.type = type #rettilineo (False) o incrocio (True)
        self.cross = cross #incrocio di cui fa parte il sector

class Graph: #intero percorso a grafo
    def __init__(self, crosses):
        self.crosses = crosses
        self.links = []
        self.saved_links = []
        for n in range(len(self.crosses)):
            tmp_cross = self.crosses[n]
            for k in tmp_cross.adjacent:
                tmp_link= tmp_cross.adjacent.get(k)[0]
                if tmp_link.id not in self.saved_links:
                    self.links.append(tmp_link)
                    self.saved_links.append(tmp_link.id)

    def findPath(self, start, goal):
        """startIsSector = False
        goalIsSector = False
        if start.__class__ == Sector().__class__: #controlla se il punto di partenza è un settore (altrimenti è un incrocio)
            startIsSector = True
            for edge in self.links:
                if start in edge.components: #trova in quale link si trova il settore
                    startDist, endDist = self.findDistanceBorderLink(start, edge.components) #calcola le distanze dai bordi del link
                    start_a = (edge.start, startDist) #salva la tupla con l'incrocio di inizio link e la distanza per raggiungerlo
                    start_b = (edge.exit, endDist) #salva la tupla con l'incrocio di fine link e la distanza per raggiungerlo
        if goal.__class__ == Sector().__class__: #controlla se il punto di arrivo è un settore (altrimenti è un incrocio)
            goalIsSector = True
            for edge in self.links:
                if goal in edge.components: #trova in quale link si trova il settore
                    startDist, endDist = self.findDistanceBorderLink(start, edge.components) #calcola le distanze dai bordi del link
                    goal_a = (edge.start, startDist) #salva la tupla con l'incrocio di inizio link e la distanza per raggiungerlo
                    goal_b = (edge.exit, endDist) #salva la tupla con l'incrocio di fine link e la distanza per raggiungerlo"""

        startIsSector = True
        goalIsSector = True
        for cross in self.crosses:
            if start == cross.id:
                startIsSector = False
                start_cross = cross #salva l'incrocio come pnto i partenza
            if goal == cross.id:
                goalIsSector = False
                goal_cross = cross #salva l'incrocio come punto di arrivo
        if startIsSector == True:
            for edge in self.links:
                """print(edge.toString())
                print("\t",edge.componentsToString())
                print("start =",start,"\n")"""
                for comp in edge.components:
                    if start == comp.id:  # trova in quale link si trova il settore
                        #print("!_!_!_!_!_TROVATO_!_!_!_!_!\n")
                        startDist, endDist = self.findDistanceBorderLink(start, edge.components)  # calcola le distanze dai bordi del link
                        start_a = (edge.start, edge.start_in, startDist)  # salva la tupla con l'incrocio di inizio link, il punto di ingresso e la distanza per raggiungerlo
                        start_b = (edge.exit, edge.exit_in, endDist)  # salva la tupla con l'incrocio di fine link, il punto di ingresso e la distanza per raggiungerlo
                        break
        if goalIsSector == True:
            for edge in self.links:
                for comp in edge.components:
                    if goal == comp.id:  # trova in quale link si trova il settore
                        startDist, endDist = self.findDistanceBorderLink(goal, edge.components)  # calcola le distanze dai bordi del link
                        goal_a = (edge.start, edge.start_in, startDist)  # salva la tupla con l'incrocio di inizio link, il punto di ingresso e la distanza per raggiungerlo
                        goal_b = (edge.exit, edge.exit_in, endDist)  # salva la tupla con l'incrocio di fine link, il punto di ingresso e la distanza per raggiungerlo
                        break


        if startIsSector == True & goalIsSector == True: #se i punti di partenza e di arrivo sono entrambi settori
            print("1) PATH",start_a[0].id, start_a[1], "---->", goal_a[0].id, goal_a[1])
            weight_1, path_1 = self.findPathCrossToCross(start_a[0].id, start_a[1], goal_a[0].id, goal_a[1]) #calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_1 = weight_1 + start_a[2] + goal_a[2] + len(path_1) #calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("1) = ",weight_1,path_1)

            print("2) PATH",start_b[0].id, start_b[1], "---->", goal_a[0].id, goal_a[1])
            weight_2, path_2 = self.findPathCrossToCross(start_b[0].id, start_b[1], goal_a[0].id, goal_a[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_2 = weight_2 + start_b[2] + goal_a[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("2) = ", weight_2, path_2)

            print("3) PATH", start_a[0].id, start_a[1], "---->", goal_b[0].id, goal_b[1])
            weight_3, path_3 = self.findPathCrossToCross(start_a[0].id, start_a[1], goal_b[0].id, goal_b[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_3 = weight_3 + start_b[2] + goal_a[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("3) = ", weight_3, path_3)

            print("4) PATH", start_b[0].id, start_b[1], "---->", goal_b[0].id, goal_b[1])
            weight_4, path_4 = self.findPathCrossToCross(start_b[0].id, start_b[1], goal_b[0].id, goal_b[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_4 = weight_4 + start_b[2] + goal_b[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("4) = ", weight_4, path_4)

        if startIsSector == True & goalIsSector == False: #se il punti di partenza è un settore e quello di arrivo è un incrocio
            print("1) PATH",start_a[0].id, start_a[1], "---->", goal_a[0].id, goal_a[1])
            weight_1, path_1 = self.findPathCrossToCross(start_a[0].id, start_a[1], goal_a[0].id, goal_a[1]) #calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_1 = weight_1 + start_a[2] + goal_a[2] + len(path_1) #calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("1) = ",weight_1,path_1)

            print("2) PATH",start_b[0].id, start_b[1], "---->", goal_a[0].id, goal_a[1])
            weight_2, path_2 = self.findPathCrossToCross(start_b[0].id, start_b[1], goal_a[0].id, goal_a[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_2 = weight_2 + start_b[2] + goal_a[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("2) = ", weight_2, path_2)

        if startIsSector == True & goalIsSector == True: #se i punti di partenza e di arrivo sono entrambi settori
            print("1) PATH",start_a[0].id, start_a[1], "---->", goal_a[0].id, goal_a[1])
            weight_1, path_1 = self.findPathCrossToCross(start_a[0].id, start_a[1], goal_a[0].id, goal_a[1]) #calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_1 = weight_1 + start_a[2] + goal_a[2] + len(path_1) #calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("1) = ",weight_1,path_1)

            print("2) PATH",start_b[0].id, start_b[1], "---->", goal_a[0].id, goal_a[1])
            weight_2, path_2 = self.findPathCrossToCross(start_b[0].id, start_b[1], goal_a[0].id, goal_a[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_2 = weight_2 + start_b[2] + goal_a[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("2) = ", weight_2, path_2)

            print("3) PATH", start_a[0].id, start_a[1], "---->", goal_b[0].id, goal_b[1])
            weight_3, path_3 = self.findPathCrossToCross(start_a[0].id, start_a[1], goal_b[0].id, goal_b[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_3 = weight_3 + start_b[2] + goal_a[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("3) = ", weight_3, path_3)

            print("4) PATH", start_b[0].id, start_b[1], "---->", goal_b[0].id, goal_b[1])
            weight_4, path_4 = self.findPathCrossToCross(start_b[0].id, start_b[1], goal_b[0].id, goal_b[1])  # calcola il percorso tra il primo incorcio di partenza e il primo incrocio di arrivo
            weight_4 = weight_4 + start_b[2] + goal_b[2] + len(path_1)  # calcola il peso complessivo di questo path aggingendo al peso trovato il peso per raggiungere i due incroci
            print("4) = ", weight_4, path_4)

        if startIsSector == False & goalIsSector == False: #se i punti di partenza e di arrivo sono entrambi incroci
            return self.findPathCrossToCross(start, goal)

    def findDistanceBorderLink(self, sector, sectors): #trova la distanza di un settore dai bordi del link
        for n in range(len(sectors)):
            if sector == sectors[n].id:
                startDist = n+1
                endDist = len(sectors)-n
                return startDist, endDist

    def findPathCrossToCross(self, start, start_in, end, end_out):
        return dijkstraCross(self.links, start, start_in, end, end_out)

    def toString(self):
        print("|------------- LINKS ---------------|")
        for n in range(len(self.links)):
            print(self.links[n].toString())


if __name__ == "__main__":
    """cross_1 = Cross('1')
    cross_2 = Cross('2')
    sector_0 = Sector("10", False)
    sector_1 = Sector("11", False)
    sector_2 = Sector("12", False)
    sector_3 = Sector("13", False)
    sector_4 = Sector("14", False)
    sector_5 = Sector("15", False)
    sector_6 = Sector("16", False)
    sector_7 = Sector("17", False)
    print("|------------- LINK a ---------------|")
    link_a = Link('a', cross_1, 'N', cross_2, 'W', [sector_0])
    print(link_a.toString())
    print(cross_1.toString())
    print(cross_2.toString())
    print("|------------- LINK b ---------------|")
    link_b = Link('b', cross_2, 'E', cross_2, 'N', [sector_1, sector_2, sector_3])
    print(link_b.toString())
    print(cross_1.toString())
    print(cross_2.toString())
    print("|------------- LINK c ---------------|")
    link_c = Link('c', cross_2, 'S', cross_1, 'E', [sector_4])
    print(link_c.toString())
    print(cross_1.toString())
    print(cross_2.toString())
    print("|------------- LINK d ---------------|")
    link_d = Link('d', cross_1, 'W', cross_1, 'S', [sector_5, sector_6, sector_7])
    print(link_d.toString())
    print(cross_1.toString())
    print(cross_2.toString())
    print("|------------- GRAPH percorso ---------------|")
    crosses = [cross_1, cross_2]
    percorso = Graph(crosses)
    print(percorso.findPathCrossToCross("1", "2"))"""

