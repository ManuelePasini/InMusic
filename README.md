# InMusic-Ontology
Questo elaborato è stato realizzato tramite Stardog, l’esecuzione di
questo IDE necessita la presenza di un server Stardog sul quale
mantenere il database. Requisiti:

-   Docker;

-   licenza d’uso di Stardog (ottenibile [qui][] e già inclusa
    all’interno della cartella ”licenze”.

Step per l’installazione:

1.  download dell’immagine di Stardog tramite Docker Hub;

                docker pull stardog/stardog:latest

2.  lanciare il server Stardog:

    ```
    docker run -it -v ~/stardog-home/:/var/opt/stardog -p 5820:5820 stardog/stardog
    ```

    ”stardog-home” è il percorso della cartella all’interno della quale
    è presente solamente la licenza d’uso di Stardog ottenuta
    precedentemente; diventerà poi la directory contenente i file
    necessari all’esecuzione del server;

3.  collegarsi a <u>[Stardog Studio][]</u> e connettersi al server
    precedentemente istanziato:

    -   **indirizzo**: http://localhost:5820;

    -   **ID**: admin;

    -   **password**: admin.

4.  spostarsi nella sezione **Databases** e creare il database
    ”InMusic”;

5.  all’interno della sezione ”Databases”, dopo aver selezionato il
    database appena creato, sotto la voce **Namespaces** importare
    tramite la funzione ”Import” in alto a sinistra il file
    ”InMusic-namespaces.ttl”;

6.  spostarsi nella sezione **Models** e tramite la funzionalità ”Create
    Model” creare un modello con le seguenti caratterstiche:

    -   **Model Name**: EmotionsInMusic-Ontology

    -   **Named Graph**: musicemotions:ontology:graph

    -   **Prefix**: music

7.  ritornare nella sezione ”Databases”, selezionare il database appena
    creato e, tramite la funzione **Load data** importare il file
    **InMusic-Ontology.ttl** specificando l’opzione **Load Data to**
    musicemotions:ontology:graph , ovvero il grafo appena creato; a
    questo punto tale grafo contiene l’ontologia;

8.  per popolare il database, selezionarlo dalla sezione ”Databases” e
    tramite la funzionalità ”Load data” importare tutti i file presenti
    nella cartella **individuals**, lasciando l’opzione ”Load Data to
    **Default Graph**”

9.  per aggiungere le query, spostarsi nella sezione Workspace ed
    effettuare l’upload dei file cliccando in alto a sinistra sul ”+”.

  [qui]: https://www.stardog.com/get-started/
  [Stardog Studio]: https://stardog.studio/