## Downloading

The library is available from the Central Maven Repository and can be used easily by many tools including Maven, Gradle, SBT, etc.

    <dependency>
      <groupId>com.connectifier.xero</groupId>
      <artifactId>client</artifactId>
      <version>0.1</version>
    </dependency>

## Example Usage

    Reader pemReader = new FileReader(new File("my-x509-cert.pem"));
    XeroClient client = new XeroClient(pemReader, consumerKey, consumerSecret);
    client.getInvoices();

## Not yet supported

Pull requests will be accepted for any not yet supported features if they are accompanied by tests. All fields in the XeroClient are protected to make it easy to extend in your own code without needing to update this client.

Many of the missing features here are due to [Xero's own XML schema library](https://github.com/XeroAPI/XeroAPI-Schemas) being horribly out-of-sync with what their API returns. Please view the bug tracker on that project for limitations.

Except for creating invoices, this client library currently only has methods for read calls. This is partially due to Xero's XML schema not specifying that some XML fragments are valid top-level elements. You'll probably have to submit a pull request to the Xero XML Schema project to be able to write new types as shown in [this example](https://github.com/benmccann/XeroAPI-Schemas/commit/334966c6fb6ef2f981a6313082b340fb18075846).

Error handling is not great because Xero's API is not conforming to their XML schema. See:
* https://github.com/XeroAPI/XeroAPI-Schemas/issues/13

Repeating invoices and bank transfers are not supported due to lack of support in Xero's own XML schemas. See:
* https://github.com/XeroAPI/XeroAPI-Schemas/issues/8
* https://github.com/XeroAPI/XeroAPI-Schemas/issues/9

Support for attachments has not yet been added.

Currently, only the private app authentication method has been implemented. We use Scribe to support OAuth, so support for the public app OAuth should be straight forward to implement if needed.

## Hacking on this library

The Gradle build tool must be installed and [Xero's XML schema library](https://github.com/XeroAPI/XeroAPI-Schemas) must be checked out as a sibling project. Build with:

    gradle compileJava

Note that you can only run via Gradle and not via an IDE. See the [JiBX binding page](http://jibx.sourceforge.net/bindcomp.html#ide-use) for more details.
