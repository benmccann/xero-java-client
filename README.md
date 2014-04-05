## Example Usage

    Reader pemReader = new FileReader(new File("my-x509-cert.pem"));
    XeroClient client = new XeroClient(pemReader, consumerKey, consumerSecret);
    client.getInvoices();

## Not yet supported

Pull requests will be accepted for any not yet supported features.

Currently, only the private app authentication method has been implemented. We use Scribe to support OAuth, so support for the public app OAuth should be straight forward to implement if needed.

Note that repeating invoices and bank transfers are not supported due to lack of support in Xero's own XML schemas. This repository will be updated when those bugs are fixed. See:
https://github.com/XeroAPI/XeroAPI-Schemas/issues/8
https://github.com/XeroAPI/XeroAPI-Schemas/issues/9

This client currently only has methods included or read calls, but it should only be a few lines to make write calls. Pull requests will be accepted to add writes. Additionally, all fields in the XeroClient are protected to make it easy to extend in your own code without needing to update this client.

## Hacking on this library

https://github.com/XeroAPI/XeroAPI-Schemas must be checked out as a sibling project
Then run:
./jaxb/generateFromSchema.sh
gradle compileJava
