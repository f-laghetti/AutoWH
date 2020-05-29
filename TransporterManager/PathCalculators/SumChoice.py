from Transporters.Transporter import TransporterInfo

#OUTPUT: restituisce una lista di liste di transporters
def best_capacity_calculator(transporter_list, target): #restituisce una lista contenente delle liste di transporter (i transporter la cui somma delle capacità è la più vicina a quella totale richiesta)
  result = []

  while len(result) == 0: #fintanto che non è stata trovata almeno una soluzione si continua ad iterare
    best_capacity_combinations(transporter_list, target, result)
    target += 1 #se non si trova una soluzione, quindi si rientra nel while, si cerca la capacità cercata aumentata di 1 così da trovare la combinazione più vicina possibile in grado di trsportare la quantità di merce

  return result #restituisce una lista di tuple ognuna contenente i transportatori la cui capacità è la somma esatta richiesta

#OUTPUT: restituisce una lista di liste di transporters
def best_capacity_combinations(transporters, target, result, partial=[]):
  s = sum_capacity(partial)

  # check if the partial sum is equals to target
  if s == target:
    result.append(partial)
  if s >= target:
    return  # if we reach the number why bother to continue

  for i in range(len(transporters)):
    n = transporters[i]
    remaining = transporters[i + 1:]
    best_capacity_combinations(remaining, target, result, partial + [n])

#data una lista di transporter, ne calcola la capacità totale
def sum_capacity(partial):
  result = 0
  if len(partial) != 0:
    for n in range(len(partial)):
      result += partial[n].capacity
  return result

#confronta la capacità totale della lista dei transporter passata con la capacità necessaria
#OUTPUT: True se la capacità richiesta è maggiore, False altrimenti
def compare_total_capacity(transporters, target):
  capacity_sum = 0
  for n in range(len(transporters)):
    capacity_sum += transporters[n].capacity
  if target > capacity_sum:
    return True
  return False

#data una lista di Shelf, restituisce il numero di livelli totali presenti
def countRemainingShelfLevels(shelves):
  tot_sum = 0
  for n in range(len(shelves)):
    tot_sum += len(shelves[n].levels)
  return tot_sum


if __name__ == "__main__":
  tr1 = TransporterInfo("aa",None,"12",True,5)
  tr2 = TransporterInfo("bb",None,"12",True,5)
  tr3 = TransporterInfo("cc",None,"12",True,3)
  tr4 = TransporterInfo("dd",None,"12",True,3)
  tr5 = TransporterInfo("ee",None,"12",True,1)
  capacity_list = [tr1,tr2,tr3,tr4,tr5]
  target = 10
  rst = best_capacity_calculator(capacity_list, target)
  print("RESULT: ")
  for n in range(len(rst)):
    strRes = ""
    for m in range(len(rst[n])):
      strRes += rst[n][m].mac_address + "-" + str(rst[n][m].capacity) + "|"
    print(strRes)

  # Outputs:
  # sum([3, 8, 4])=15
  # sum([3, 5, 7])=15
  # sum([8, 7])=15
  # sum([5, 10])=15

