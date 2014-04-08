// Copyright 2014 Connectifier, Inc. All Rights Reserved.

package com.connectifier.xeroclient;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import com.connectifier.xeroclient.models.BankTransaction;
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
import com.connectifier.xeroclient.models.Organisation;
import com.connectifier.xeroclient.models.Payment;
import com.connectifier.xeroclient.models.Receipt;
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

  protected final OAuthService service;
  protected final Token token;
  
  public XeroClient(Reader pemReader, String consumerKey, String consumerSecret) {
    service = new ServiceBuilder()
        .provider(new XeroOAuthService(pemReader))
        .apiKey(consumerKey)
        .apiSecret(consumerSecret)
        .build();
    token = new Token(consumerKey, consumerSecret);
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
      ApiException exception = unmarshallResponse(response, ApiException.class);
      throw new XeroApiException(response.getCode() + " response: Error number "
          + exception.getErrorNumber() + ". " + exception.getMessage());        
    }
    return unmarshallResponse(response, ResponseType.class);
  }

  private <T> T unmarshallResponse(Response response, Class<T> clazz) {
    try {
      JAXBContext context = JAXBContext.newInstance(clazz);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Source source = new StreamSource(new ByteArrayInputStream(response.getBody().getBytes()));
      return unmarshaller.unmarshal(source, clazz).getValue();
    } catch (JAXBException e) {
      throw new IllegalStateException(e);
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

// https://github.com/XeroAPI/XeroAPI-Schemas/issues/8
//  public List<BankTransfer> getBankTransfers() {
//    return issueQuery("BankTransfers").getankTransfers().getBankTransfer();
//  }

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

// https://github.com/XeroAPI/XeroAPI-Schemas/issues/9
//  public List<RepeatingInvoice> getRepeatingInvoices() {
//    return issueQuery("RepeatingInvoices").getRepeatingInvoices().getRepeatingInvoice();
//  }

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
