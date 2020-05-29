from Space.Shelves import Shelf

def clean_POST_data(data):
    return data[2:-1]

def parse_POST_command(data):
    listed_command = []
    command = clean_POST_data(data)
    tmp_list = command.split('&')
    if tmp_list[0] == "CONNECT":
        print("Transporters connection requested")
    elif tmp_list[0] == "DELIVER_SHELF" or tmp_list[0] == "RETRIEVE_SHELF":
        print(tmp_list[0],"order requested")
        #tmp_list[1] #id dell'ordine
        #tmp_list[2] #coordinate del transactor
        #tmp_list[3] #numero di merci da riprendere
        tmp_shelves = parse_shelf(tmp_list[4])
        tmp_list = tmp_list[:4]
        tmp_list.append(tmp_shelves)
    else:
        print("ERROR: unknown command")
    return tmp_list

def parse_shelf(shelves):
    shelves_list = shelves.split('$') #divide tutte le shelf
    shelves_info = []
    for n in range(len(shelves_list)):
        tmp_info = shelves_list[n].split('|') #all'interno di una shelf, divide la coordinata dai livelli della shelf
        tmp_levels = tmp_info[1].split('/') #divide i vari livelli della shelf
        #tmp_info = tmp_info[:1] #salva la coordinata della shelf
        #tmp_info.append(tmp_levels) #aggiunge i livelli alla coordinata corrispondente
        tmp_shelf = Shelf(tmp_info[0],tmp_levels)
        shelves_info.append(tmp_shelf) #aggiunge l'info così creata alla lista generale delle info sulle shelf
    return shelves_info
