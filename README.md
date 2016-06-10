## Unmaintained - Forks Welcomed

We will no longer be maintaining this repository, so please feel free to fork it.

## Downloading

The library is available from the Central Maven Repository and can be used easily by many tools including Maven, Gradle, SBT, etc.

    <dependency>
      <groupId>com.connectifier.xero</groupId>
      <artifactId>client</artifactId>
      <version>0.13</version>
    </dependency>

## Example Usage

Creating a client:

    Reader pemReader = new FileReader(new File("my-x509-cert.pem"));
    XeroClient client = new XeroClient(pemReader, consumerKey, consumerSecret);

Getting invoices:

    client.getInvoices();

Creating an invoice:

    Invoice invoice = new Invoice();
    invoice.setType(InvoiceType.ACCREC);
    invoice.setContact(client.getContacts().get(0));
    LineItem item = new LineItem();
    item.setDescription("Test Invoice");
    invoice.setLineItems(ImmutableList.of(item));
    client.createInvoice(invoice);

## Advantages

* This library and all dependencies are available in the Maven Central Repository
* Compatible with Java 8 (libraries using JiBX are not due to BCEL dependency)
* Work with standard Java classes like List&lt;Invoice&gt; and Date (other libraries using JAXB give you things like ArrayOfInvoice and JAXBElement&lt;GregorianCalendar&gt;)
* Easily extensible - protected, non-final client

## Not yet supported

Pull requests will be accepted for any not yet supported features. Changes to existing methods will be more likely to be accepted if a test is added. All fields in the XeroClient are protected to make it easy to extend in your own code without needing to update this client.

The only write call included thus far is for creating invoices and receipts because that's all we've needed. It's only a few lines to add a new write method if you find one that you need. However, this is another area where Xero's XML schemas are lacking, so you may have to submit a pull request to the Xero XML Schema project to be able to write new types as shown in [this example](https://github.com/benmccann/XeroAPI-Schemas/commit/334966c6fb6ef2f981a6313082b340fb18075846) and [this example](https://github.com/XeroAPI/XeroAPI-Schemas/commit/58d1fdd66b5f8024d8a3e35b18fb0a563211588a).

Support for attachments has not yet been added.

Currently, only the private app authentication method has been implemented. We use Scribe to support OAuth, so support for the public app OAuth should be straight forward to implement if needed.

## Hacking on this library

The SBT build tool must be installed and [Xero's XML schema library](https://github.com/XeroAPI/XeroAPI-Schemas) must be checked out as a sibling project. Build with:

    sbt compile

