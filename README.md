# QA-SRL Bank Scala Client

This directory houses the Scala client library for the QA-SRL Bank 2.0 dataset.
In here are types and JSON serialization tools for reading in the data and
processing it or serving it through HTTP.

## Usage

Add the following to your Mill build `ivyDeps`:
```scala
  ivy"org.julianmichael::qasrl-bank::0.1.0",
  ivy"org.julianmichael::qasrl-bank-service::0.1.0" // optional
```
The main way you would use this is construct a `qasrl.bank.FullData` object
(defined [here](qasrl-bank/src-jvm/qasrl/bank/Data.scala)) passing it the
location of the directory holding the QA-SRL Bank data. If you also include
the `qasrl-bank-service` dependency, you can use this to spin up an
[HTTP server](qasrl-bank-service/src-jvm/DocumentServiceWebServer.scala)
that you can then hit with requests from the
[JavaScript client](qasrl-bank-service/src-js/WebClientDocumentService.scala)
in the browser.
For example uses, see the [qasrl-apps](https://github.com/julianmichael/qasrl-apps) project.
