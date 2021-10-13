## Language Detection Server

### Usage

Very quick instructions (you need access to language fingerprint files)

Run the server

Add fingerprint files for languages you care about:

```shell
curl -XPUT --data @fr-FR.fp  http://localhost:8080/fr-FR
```

... where the name you use in the URL is the name that you want returned for that fingerprint file

Post a text file to it, it should respond with the identified language

```shell
curl -XPOST --data @test.txt http://localhost:8080/
```

Eagle-eyed folks may spot similarities to my XsltServer project...
