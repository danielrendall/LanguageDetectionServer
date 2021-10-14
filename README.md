## Language Detection Server

### Usage

Very quick instructions

Run the server

Create a fingerprint file

```shell
curl -XPOST --data @some_french_text_document.txt http://localhost:8080/_fingerprint > fr-FR.fp
```

Add that fingerprint file:

```shell
curl -XPUT --data @fr-FR.fp  http://localhost:8080/fr-FR
```

... where the name you use in the URL is the name that you want returned for that fingerprint file

Create and add fingerprints for any other languages.

Post a text file to it, it should respond with the identified language

```shell
curl -XPOST --data @test.txt http://localhost:8080/_detect
```

Eagle-eyed folks may spot similarities to my XsltServer project...
