// Copyright 2014 Connectifier, Inc. All Rights Reserved.

package com.connectifier.xeroclient;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.connectifier.xeroclient.models.Account;
import com.connectifier.xeroclient.models.ApiException;
import com.connectifier.xeroclient.models.ArrayOfInvoice;
import com.connectifier.xeroclient.models.ArrayOfBankTransaction;
import com.connectifier.xeroclient.models.ArrayOfManualJournal;
import com.connectifier.xeroclient.models.BankTransaction;
import com.connectifier.xeroclient.models.BankTransfer;
import com.connectifier.xeroclient.models.BrandingTheme;
import com.connectifier.xeroclient.models.Contact;
import com.connectifier.xeroclient.models.CreditNote;
import com.connectifier.xeroclient.models.Currency;
import com.connectifier.xeroclient.models.Employee;
import com.connectifier.xeroclient.models.ExpenseClaim;
import com.connectifier.xeroclient.models.Invoice;
import com.connectifier.xeroclient.models.Item;
import com.connectifier.xeroclient.models.Journal;
import com.connectifier.xeroclient.models.ManualJournal;
import com.connectifier.xeroclient.models.ObjectFactory;
import com.connectifier.xeroclient.models.Organisation;
import com.connectifier.xeroclient.models.Payment;
import com.connectifier.xeroclient.models.Receipt;
import com.connectifier.xeroclient.models.RepeatingInvoice;
import com.connectifier.xeroclient.models.ResponseType;
import com.connectifier.xeroclient.models.TaxRate;
import com.connectifier.xeroclient.models.TrackingCategory;
import com.connectifier.xeroclient.models.User;
import com.connectifier.xeroclient.oauth.XeroOAuthService;

public class XeroClient {

  protected static final String BASE_URL = "https://api.xero.com/api.xro/2.0/";
  protected static final DateFormat utcFormatter;
  static {
    utcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  protected static final Pattern MESSAGE_PATTERN = Pattern.compile("<Message>(.*)</Message>");

  protected final OAuthService service;
  protected final Token token;
  protected final ObjectFactory objFactory = new ObjectFactory();

  public XeroClient(Reader pemReader, String consumerKey, String consumerSecret) {
    service = new ServiceBuilder()
        .provider(new XeroOAuthService(pemReader))
        .apiKey(consumerKey)
        .apiSecret(consumerSecret)
        .build();
    token = new Token(consumerKey, consumerSecret);
  }

  protected XeroApiException newApiException(Response response) {
    ApiException exception = null;
    try {
      exception = unmarshallResponse(response.getBody(), ApiException.class);
    } catch (Exception e) {
    }
    // Jibx doesn't support xsi:type, so we pull out errors this somewhat-hacky way 
    Matcher matcher = MESSAGE_PATTERN.matcher(response.getBody());
    StringBuilder messages = new StringBuilder();
    while (matcher.find()) {
      if (messages.length() > 0) {
        messages.append(", ");
      }
      messages.append(matcher.group(1));
    }
    if (exception == null) {
      if (messages.length() > 0) {
        return new XeroApiException(response.getCode(), messages.toString());
      }
      return new XeroApiException(response.getCode());
    }
    return new XeroApiException(response.getCode(), "Error number " + exception.getErrorNumber() + ". " + messages);     
  }
  
  protected ResponseType get(String endPoint) {
    return get(endPoint, null, null);
  }

  protected ResponseType get(String endPoint, Date modifiedAfter, Map<String,String> params) {
    OAuthRequest request = new OAuthRequest(Verb.GET, BASE_URL + endPoint);
    if (modifiedAfter != null) {
      request.addHeader("If-Modified-Since", utcFormatter.format(modifiedAfter));
    }
    if (params != null) {
      for (Map.Entry<String,String> param : params.entrySet()) {
        request.addQuerystringParameter(param.getKey(), param.getValue());
      }
    }
    service.signRequest(token, request);
    Response response = request.send();
    if (response.getCode() != 200) {
      throw newApiException(response);
    }
    return unmarshallResponse(response.getBody(), ResponseType.class);
  }

  protected ResponseType put(String endPoint, JAXBElement<?> object) {
    OAuthRequest request = new OAuthRequest(Verb.PUT, BASE_URL + endPoint);
    String contents = marshallRequest(object);
    request.setCharset("UTF-8");
    request.addBodyParameter("xml", contents);
    service.signRequest(token, request);
    Response response = request.send();
    if (response.getCode() != 200) {
      throw newApiException(response);
    }
    return unmarshallResponse(response.getBody(), ResponseType.class);
  }

  protected ResponseType post(String endPoint, JAXBElement<?> object) {
    OAuthRequest request = new OAuthRequest(Verb.POST, BASE_URL + endPoint);
    String contents = marshallRequest(object);
    request.setCharset("UTF-8");
    request.addBodyParameter("xml", contents);
    service.signRequest(token, request);
    Response response = request.send();
    if (response.getCode() != 200) {
      throw newApiException(response);
    }
    return unmarshallResponse(response.getBody(), ResponseType.class);
  }

  protected <T> String marshallRequest(JAXBElement<?> object) {
    try {
      JAXBContext context = JAXBContext.newInstance(object.getValue().getClass());
      Marshaller marshaller = context.createMarshaller();
      StringWriter writer = new StringWriter();
      marshaller.marshal(object, writer);
      return writer.toString();
    } catch (JAXBException e) {
      throw new IllegalStateException("Error marshalling request object " + object.getClass(), e);
    }
  }

  protected static <T> T unmarshallResponse(String responseBody, Class<T> clazz) {
    try {
      JAXBContext context = JAXBContext.newInstance(clazz);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Source source = new StreamSource(new ByteArrayInputStream(responseBody.getBytes()));
      return unmarshaller.unmarshal(source, clazz).getValue();
    } catch (JAXBException e) {
      throw new IllegalStateException("Error unmarshalling response: " + responseBody, e);
    }
  }

  protected void addToMapIfNotNull(Map<String,String> map, String key, Object value) {
    if (value != null) {
      map.put(key, value.toString());
    }
  }
  
  protected <T> T singleResult(List<T> list) {
    if (list.isEmpty()) {
      return null;
    }
    if (list.size() > 1) {
      throw new IllegalStateException("Got multiple results for query");
    }
    return list.get(0);
  }

  public Account getAccount(String id) {
    return singleResult(get("Accounts/" + id).getAccounts());
  }

  public List<Account> getAccounts() {
    return get("Accounts").getAccounts();
  }

  public List<Account> getAccounts(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Accounts", modifiedAfter, params).getAccounts();
  }

  public BankTransaction getBankTransaction(String id) {
    return singleResult(get("BankTransactions/" + id).getBankTransactions());
  }

  public List<BankTransaction> getBankTransactions() {
    return get("BankTransactions").getBankTransactions();
  }

  public List<BankTransaction> getBankTransactions(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("BankTransactions", modifiedAfter, params).getBankTransactions();
  }

  public List<BankTransaction> createBankTransactions(List<BankTransaction> bankTransactions) {
    ArrayOfBankTransaction array = new ArrayOfBankTransaction();
    array.getBankTransaction().addAll(bankTransactions);
    return put("BankTransactions", objFactory.createBankTransactions(array)).getBankTransactions();
  }

  public List<BankTransfer> getBankTransfers() {
    return get("BankTransfers").getBankTransfers();
  }

  public List<BrandingTheme> getBrandingThemes() {
    return get("BrandingThemes").getBrandingThemes();
  }

  public List<BankTransaction> getBrandingThemes(String name, Integer sortOrder, Date createdDateUTC) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Name", name);
    addToMapIfNotNull(params, "sortOrder", sortOrder);
    if (createdDateUTC != null) {
      params.put("CreatedDateUTC", utcFormatter.format(createdDateUTC));
    }
    return get("BankTransactions", null, params).getBankTransactions();
  }

  public Contact getContact(String id) {
    return singleResult(get("Contacts/" + id).getContacts());
  }

  public List<Contact> getContacts() {
    return get("Contacts").getContacts();
  }

  public List<Contact> getContacts(Date modifiedAfter, String where, String order, Integer page, Boolean includedArchive) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    addToMapIfNotNull(params, "page", page);
    addToMapIfNotNull(params, "includeArchived", includedArchive);
    return get("Contacts", modifiedAfter, params).getContacts();
  }

  public CreditNote getCreditNote(String id) {
    return singleResult(get("CreditNotes/" + id).getCreditNotes());
  }

  public List<CreditNote> getCreditNotes() {
    return get("CreditNotes").getCreditNotes();
  }

  public List<CreditNote> getCreditNotes(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("CreditNotes", modifiedAfter, params).getCreditNotes();
  }

  public List<Currency> getCurrencies() {
    return get("Currencies").getCurrencies();
  }

  public Employee getEmployee(String id) {
    return singleResult(get("Employees/" + id).getEmployees());
  }

  public List<Employee> getEmployees() {
    return get("Employees").getEmployees();
  }

  public List<Employee> getEmployees(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Employees", modifiedAfter, params).getEmployees();
  }

  public List<ExpenseClaim> getExpenseClaim(String id) {
    return get("ExpenseClaims/" + id).getExpenseClaims();
  }

  public List<ExpenseClaim> getExpenseClaims() {
    return get("ExpenseClaims").getExpenseClaims();
  }

  public List<ExpenseClaim> getExpenseClaims(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("ExpenseClaims", modifiedAfter, params).getExpenseClaims();
  }

  public Invoice getInvoice(String id) {
    return singleResult(get("Invoices/" + id).getInvoices());
  }

  public List<Invoice> getInvoices() {
    return get("Invoices").getInvoices();
  }

  public List<Invoice> getInvoices(Date modifiedAfter, String where, String order, Integer page) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    addToMapIfNotNull(params, "page", page);
    return get("Invoices", modifiedAfter, params).getInvoices();
  }

  public List<Invoice> createInvoice(Invoice invoice) {
    return put("Invoices", objFactory.createInvoice(invoice)).getInvoices();
  }

  public List<Invoice> updateInvoice(Invoice invoice) {
    return post("Invoices", objFactory.createInvoice(invoice)).getInvoices();
  }

  public List<Receipt> createReceipt(Receipt receipt) {
    return put("Receipts", objFactory.createReceipt(receipt)).getReceipts();
  }

  public List<Invoice> createInvoices(List<Invoice> invoices) {
    ArrayOfInvoice array = new ArrayOfInvoice();
    array.getInvoice().addAll(invoices);
    return put("Invoices", objFactory.createInvoices(array)).getInvoices();
  }

  public List<Invoice> updateInvoices(List<Invoice> invoices) {
    ArrayOfInvoice array = new ArrayOfInvoice();
    array.getInvoice().addAll(invoices);
    return post("Invoices", objFactory.createInvoices(array)).getInvoices();
  }

  public Item getItem(String id) {
    return singleResult(get("Items/" + id).getItems());
  }

  public List<Item> getItems() {
    return get("Items").getItems();
  }

  public List<Item> getItems(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Items", modifiedAfter, params).getItems();
  }

  public Journal getJournal(String id) {
    return singleResult(get("Journal").getJournals());
  }

  public List<Journal> getJournals() {
    return get("Journals").getJournals();
  }

  public List<Journal> getJournals(Date modifiedAfter, Integer offset, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "offset", offset);
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Journals", modifiedAfter, params).getJournals();
  }

  public ManualJournal getManualJournal(String id) {
    return singleResult(get("ManualJournals/" + id).getManualJournals());
  }

  public List<ManualJournal> getManualJournals() {
    return get("ManualJournals").getManualJournals();
  }

  public List<ManualJournal> getManualJournals(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("ManualJournal", modifiedAfter, params).getManualJournals();
  }

  public List<ManualJournal> createManualJournals(List<ManualJournal> manualJournals) {
    ArrayOfManualJournal array = new ArrayOfManualJournal();
    array.getManualJournal().addAll(manualJournals);
    return put("ManualJournals", objFactory.createManualjournals(array)).getManualJournals();
  }

  public Organisation getOrganisation() {
    return singleResult(get("Organisation").getOrganisations());
  }

  public Payment getPayments(String id) {
    return singleResult(get("Payments/" + id).getPayments());
  }

  public List<Payment> getPayments() {
    return get("Payments").getPayments();
  }

  public List<Payment> getPayments(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Payments", modifiedAfter, params).getPayments();
  }

  public Receipt getReceipt(String id) {
    return singleResult(get("Receipts/" + id).getReceipts());
  }

  public List<Receipt> getReceipts() {
    return get("Receipts").getReceipts();
  }

  public List<Receipt> getReceipts(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Receipts", modifiedAfter, params).getReceipts();
  }

  public List<RepeatingInvoice> getRepeatingInvoices() {
    return get("RepeatingInvoices").getRepeatingInvoices();
  }

  public List<TaxRate> getTaxRates() {
    return get("TaxRates").getTaxRates();
  }

  public List<TaxRate> getTaxRates(String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("TaxRates", null, params).getTaxRates();
  }

  public TrackingCategory getTrackingCategory(String id) {
    return singleResult(get("TrackingCategories/" + id).getTrackingCategories());
  }

  public List<TrackingCategory> getTrackingCategories() {
    return get("TrackingCategories").getTrackingCategories();
  }

  public List<TrackingCategory> getTrackingCategories(String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("TrackingCategories", null, params).getTrackingCategories();
  }

  public User getUser(String id) {
    return singleResult(get("Users/" + id).getUsers());
  }

  public List<User> getUsers() {
    return get("Users").getUsers();
  }

  public List<User> getUsers(Date modifiedAfter, String where, String order) {
    Map<String, String> params = new HashMap<>();
    addToMapIfNotNull(params, "Where", where);
    addToMapIfNotNull(params, "order", order);
    return get("Users", modifiedAfter, params).getUsers();
  }

}
