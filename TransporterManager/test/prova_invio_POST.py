import requests

url = 'http://localhost:8080'
payload =  "prova"

r = requests.post(url, data=payload) # Note data is used here
print(r.text) # Prints empty string