package com.poc.recommender.datamodel;

/** This class contain source code from MongoDataModel class. Since MongoDataModel Class is Final. The Source code has been used.
 *  The code build method was modified to meet the end requirement.
 *  
 */

import com.google.common.base.Preconditions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;



public  class RecommenderDataModel implements DataModel {


private static final long serialVersionUID = -5850904872484413637L;

private static final Logger log = LoggerFactory.getLogger(RecommenderDataModel.class);
  
 private MongoOperations mongoOp;

 
  
  private static final String DEFAULT_MONGO_COLLECTION = "user_item_view";
  private static final boolean DEFAULT_MONGO_MANAGE = true;
  private static final String DEFAULT_MONGO_USER_ID = "user_id";
  private static final String DEFAULT_MONGO_ITEM_ID = "item_id";
  private static final String DEFAULT_MONGO_PREFERENCE = "preference";
  private static final boolean DEFAULT_MONGO_FINAL_REMOVE = false;
  private static final DateFormat DEFAULT_DATE_FORMAT =
      new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss 'GMT'Z (zzz)", Locale.ENGLISH);

  public static final String DEFAULT_MONGO_MAP_COLLECTION = "mongo_data_model_map";

  private static final Pattern ID_PATTERN = Pattern.compile("[a-f0-9]{24}");
  private String mongoCollection = DEFAULT_MONGO_COLLECTION;
  private String mongoMapCollection = DEFAULT_MONGO_MAP_COLLECTION;
  private boolean mongoManage = DEFAULT_MONGO_MANAGE;
  private String mongoUserID = DEFAULT_MONGO_USER_ID;
  private String mongoItemID = DEFAULT_MONGO_ITEM_ID;
  private String mongoPreference = DEFAULT_MONGO_PREFERENCE;
  private boolean mongoFinalRemove = DEFAULT_MONGO_FINAL_REMOVE;
  private DateFormat dateFormat = DEFAULT_DATE_FORMAT;
  private MongoCollection<Document> collection;
  private MongoCollection<Document> collectionMap;
  private Date mongoTimestamp;
  private final ReentrantLock reloadLock;
  private DataModel delegate;
  private boolean userIsObject;
  private boolean itemIsObject;
  private boolean preferenceIsString;
  private long idCounter;

  /**
   * Creates a new MongoDBDataModel
   */
  public RecommenderDataModel(MongoOperations mongoOps) throws UnknownHostException {
	this.mongoOp = mongoOps;
    this.reloadLock = new ReentrantLock();
    buildModel();
  }

 
  /**
   * <p>
   * Adds/removes (user, item) pairs to/from the model.
   * </p>
   *
   * @param userID MongoDB user identifier
   * @param items  List of pairs (item, preference) which want to be added or
   *               deleted
   * @param add    If true, this flag indicates that the    pairs (user, item)
   *               must be added to the model. If false, it indicates deletion.
   * @see #refresh(Collection)
   */
  public void refreshData(String userID,
                          Iterable<List<String>> items,
                          boolean add) throws NoSuchUserException, NoSuchItemException {
    checkData(userID, items, add);
    long id = Long.parseLong(fromIdToLong(userID, true));
    for (List<String> item : items) {
      item.set(0, fromIdToLong(item.get(0), false));
    }
    if (reloadLock.tryLock()) {
      try {
        if (add) {
          delegate = addUserItem(id, items);
        } else {
          delegate = removeUserItem(id, items);
        }
      } finally {
        reloadLock.unlock();
      }
    }
  }


  /**
   * <p>
   * Triggers "refresh" -- whatever that means -- of the implementation.
   * The general contract is that any should always leave itself in a
   * consistent, operational state, and that the refresh atomically updates
   * internal state from old to new.
   * </p>
   *
   * @param alreadyRefreshed s that are known to have already been refreshed as
   *                         a result of an initial call to a method on some object. This ensures
   *                         that objects in a refresh dependency graph aren't refreshed twice
   *                         needlessly.
   * @see #refreshData(String, Iterable, boolean)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    Document query = new Document();
    query.put("deleted_at", new Document("$gt", mongoTimestamp));
    MongoCursor<Document> cursor = collection.find().cursor();
    Date ts = new Date(0);
    while (cursor.hasNext()) {
      Map<String,Object> user = cursor.next();
      String userID = getID(user.get(mongoUserID), true);
      Collection<List<String>> items = new ArrayList<>();
      List<String> item = new ArrayList<>();
      item.add(getID(user.get(mongoItemID), false));
      item.add(Float.toString(getPreference(user.get(mongoPreference))));
      items.add(item);
      try {
        refreshData(userID, items, false);
      } catch (NoSuchUserException e) {
        log.warn("No such user ID: {}", userID);
      } catch (NoSuchItemException e) {
        log.warn("No such items: {}", items);
      }
      if (ts.compareTo(getDate(user.get("created_at"))) < 0) {
        ts = getDate(user.get("created_at"));
      }
    }
    query = new Document();
    query.put("created_at", new Document("$gt", mongoTimestamp));
    cursor = collection.find(query).cursor();
    while (cursor.hasNext()) {
      Map<String,Object> user = cursor.next();
      if (!user.containsKey("deleted_at")) {
        String userID = getID(user.get(mongoUserID), true);
        Collection<List<String>> items = new ArrayList<>();
        List<String> item = new ArrayList<>();
        item.add(getID(user.get(mongoItemID), false));
        item.add(Float.toString(getPreference(user.get(mongoPreference))));
        items.add(item);
        try {
          refreshData(userID, items, true);
        } catch (NoSuchUserException e) {
          log.warn("No such user ID: {}", userID);
        } catch (NoSuchItemException e) {
          log.warn("No such items: {}", items);
        }
        if (ts.compareTo(getDate(user.get("created_at"))) < 0) {
          ts = getDate(user.get("created_at"));
        }
      }
    }
    if (mongoTimestamp.compareTo(ts) < 0) {
      mongoTimestamp = ts;
    }
  }

  /**
   * <p>
   * Translates the MongoDB identifier to Mahout/MongoDBDataModel's internal
   * identifier, if required.
   * </p>
   * <p>
   * If MongoDB identifiers are long datatypes, it returns the id.
   * </p>
   * <p>
   * This conversion is needed since Mahout uses the long datatype to feed the
   * recommender, and MongoDB uses 12 bytes to create its identifiers.
   * </p>
   *
   * @param id     MongoDB identifier
   * @param isUser
   * @return String containing the translation of the external MongoDB ID to
   *         internal long ID (mapping).
   * @see #fromLongToId(long)
   * @see <a href="http://www.mongodb.org/display/DOCS/Object%20IDs">
   *      Mongo Object IDs</a>
   */
  public String fromIdToLong(String id, boolean isUser) {
	 FindIterable<Document> itr = collectionMap.find(new Document("element_id", id));
	 Document objectIdLong = itr.first();
    if (objectIdLong != null) {
      Map<String,Object> idLong = (Map<String,Object>) objectIdLong;
      Object value = idLong.get("long_value");
      return value == null ? null : value.toString();
    } else {
      objectIdLong = new Document();
      String longValue = Long.toString(idCounter++);
      objectIdLong.put("element_id", id);
      objectIdLong.put("long_value", longValue);
      collectionMap.insertOne(objectIdLong);
      log.info("Adding Translation {}: {} long_value: {}",
               isUser ? "User ID" : "Item ID", id, longValue);
      return longValue;
    }
  }

  /**
   * <p>
   * Translates the Mahout/MongoDBDataModel's internal identifier to MongoDB
   * identifier, if required.
   * </p>
   * <p>
   * If MongoDB identifiers are long datatypes, it returns the id in String
   * format.
   * </p>
   * <p>
   * This conversion is needed since Mahout uses the long datatype to feed the
   * recommender, and MongoDB uses 12 bytes to create its identifiers.
   * </p>
   *
   * @param id Mahout's internal identifier
   * @return String containing the translation of the internal long ID to
   *         external MongoDB ID (mapping).
   * @see #fromIdToLong(String, boolean)
   * @see <a href="http://www.mongodb.org/display/DOCS/Object%20IDs">
   *      Mongo Object IDs</a>
   */
  public String fromLongToId(long id) {
    Document objectIdLong = collectionMap.find(new Document("long_value", Long.toString(id))).first();
    Map<String,Object> idLong = (Map<String,Object>) objectIdLong;
    Object value = idLong.get("element_id");
    return value == null ? null : value.toString();
  }

  /**
   * <p>
   * Checks if an ID is currently in the model.
   * </p>
   *
   * @param ID user or item ID
   * @return true: if ID is into the model; false: if it's not.
   */
  public boolean isIDInModel(String ID) {
    Document objectIdLong = collectionMap.find(new Document("element_id", ID)).first();
    return objectIdLong != null;
  }

  /**
   * <p>
   * Date of the latest update of the model.
   * </p>
   *
   * @return Date with the latest update of the model.
   */
  public Date mongoUpdateDate() {
    return mongoTimestamp;
  }

  private void buildModel() throws UnknownHostException {
    userIsObject = false;
    itemIsObject = false;
    idCounter = 0;
    preferenceIsString = true;
   
    mongoTimestamp = new Date(0);
    FastByIDMap<Collection<Preference>> userIDPrefMap = new FastByIDMap<>();
    if (mongoOp !=null) {
      MongoCollection<Document> collection = mongoOp.getCollection(mongoCollection);
      MongoCollection<Document> collectionMap = mongoOp.getCollection(mongoMapCollection);
      
      Document indexObj = new Document();
      indexObj.put("element_id", 1);
      Document indexObj2 = new Document();
      indexObj2.put("long_value", 1);
      collectionMap.createIndex(indexObj);
      collectionMap.createIndex(indexObj2);
      // TODO Here in find() we need to pass query Document for getting view data more than 3 months
      FindIterable<Document> it = collection.find();
      MongoCursor<Document> cursor =  it.cursor();
      while (cursor.hasNext()) {
          Map<String,Object> user = cursor.next();
          if (!user.containsKey("deleted_at")) {
            long userID = (long) user.get(mongoUserID);
            long itemID = (long) user.get(mongoItemID);
            float ratingValue = getPreference(user.get(mongoPreference));
            Collection<Preference> userPrefs = userIDPrefMap.get(userID);
            if (userPrefs == null) {
              userPrefs = new ArrayList<>(2);
              userIDPrefMap.put(userID, userPrefs);
            }
            userPrefs.add(new GenericPreference(userID, itemID, ratingValue));
            if (user.containsKey("created_at")
                && mongoTimestamp.compareTo(getDate(user.get("created_at"))) < 0) {
              mongoTimestamp = getDate(user.get("created_at"));
            }
          }
        }
    }
    delegate = new GenericDataModel(GenericDataModel.toDataMap(userIDPrefMap, true));
  }

  private void removeMongoUserItem(String userID, String itemID) {
    String userId = fromLongToId(Long.parseLong(userID));
    String itemId = fromLongToId(Long.parseLong(itemID));
    if (isUserItemInDB(userId, itemId)) {
      mongoTimestamp = new Date();
      Document query = new Document();
      query.put(mongoUserID, userIsObject ? new ObjectId(userId) : userId);
      query.put(mongoItemID, itemIsObject ? new ObjectId(itemId) : itemId);
      if (mongoFinalRemove) {
   
        log.info(collection.findOneAndDelete(query).toString());
      } else {
        Document update = new Document();
        update.put("$set", new Document("deleted_at", mongoTimestamp));
        log.info(collection.updateOne(query, update).toString());
      }
      log.info("Removing userID: {} itemID: {}", userID, itemId);
    }
  }

  private void addMongoUserItem(String userID, String itemID, String preferenceValue) {
    String userId = fromLongToId(Long.parseLong(userID));
    String itemId = fromLongToId(Long.parseLong(itemID));
    if (!isUserItemInDB(userId, itemId)) {
      mongoTimestamp = new Date();
      Document user = new Document();
      Object userIdObject = userIsObject ? new ObjectId(userId) : userId;
      Object itemIdObject = itemIsObject ? new ObjectId(itemId) : itemId;
      user.put(mongoUserID, userIdObject);
      user.put(mongoItemID, itemIdObject);
      user.put(mongoPreference, preferenceIsString ? preferenceValue : Double.parseDouble(preferenceValue));
      user.put("created_at", mongoTimestamp);
      collection.insertOne(user);
      log.info("Adding userID: {} itemID: {} preferenceValue: {}", userID, itemID, preferenceValue);
    }
  }

  private boolean isUserItemInDB(String userID, String itemID) {
    Document query = new Document();
    Object userId = userIsObject ? new ObjectId(userID) : userID;
    Object itemId = itemIsObject ? new ObjectId(itemID) : itemID;
    query.put(mongoUserID, userId);
    query.put(mongoItemID, itemId);
    return collection.find(query).first() != null;
  }

  private DataModel removeUserItem(long userID, Iterable<List<String>> items) {
    FastByIDMap<PreferenceArray> rawData = ((GenericDataModel) delegate).getRawUserData();
    for (List<String> item : items) {
      PreferenceArray prefs = rawData.get(userID);
      long itemID = Long.parseLong(item.get(0));
      if (prefs != null) {
        boolean exists = false;
        int length = prefs.length();
        for (int i = 0; i < length; i++) {
          if (prefs.getItemID(i) == itemID) {
            exists = true;
            break;
          }
        }
        if (exists) {
          rawData.remove(userID);
          if (length > 1) {
            PreferenceArray newPrefs = new GenericUserPreferenceArray(length - 1);
            for (int i = 0, j = 0; i < length; i++, j++) {
              if (prefs.getItemID(i) == itemID) {
                j--;
              } else {
                newPrefs.set(j, prefs.get(i));
              }
            }
            rawData.put(userID, newPrefs);
          }
          log.info("Removing userID: {} itemID: {}", userID, itemID);
          if (mongoManage) {
            removeMongoUserItem(Long.toString(userID), Long.toString(itemID));
          }
        }
      }
    }
    return new GenericDataModel(rawData);
  }

  private DataModel addUserItem(long userID, Iterable<List<String>> items) {
    FastByIDMap<PreferenceArray> rawData = ((GenericDataModel) delegate).getRawUserData();
    PreferenceArray prefs = rawData.get(userID);
    for (List<String> item : items) {
      long itemID = Long.parseLong(item.get(0));
      float preferenceValue = Float.parseFloat(item.get(1));
      boolean exists = false;
      if (prefs != null) {
        for (int i = 0; i < prefs.length(); i++) {
          if (prefs.getItemID(i) == itemID) {
            exists = true;
            prefs.setValue(i, preferenceValue);
            break;
          }
        }
      }
      if (!exists) {
        if (prefs == null) {
          prefs = new GenericUserPreferenceArray(1);
        } else {
          PreferenceArray newPrefs = new GenericUserPreferenceArray(prefs.length() + 1);
          for (int i = 0, j = 1; i < prefs.length(); i++, j++) {
            newPrefs.set(j, prefs.get(i));
          }
          prefs = newPrefs;
        }
        prefs.setUserID(0, userID);
        prefs.setItemID(0, itemID);
        prefs.setValue(0, preferenceValue);
        log.info("Adding userID: {} itemID: {} preferenceValue: {}", userID, itemID, preferenceValue);
        rawData.put(userID, prefs);
        if (mongoManage) {
          addMongoUserItem(Long.toString(userID),
                           Long.toString(itemID),
                           Float.toString(preferenceValue));
        }
      }
    }
    return new GenericDataModel(rawData);
  }

  private Date getDate(Object date) {
    if (date.getClass().getName().contains("Date")) {
      return (Date) date;
    }
    if (date.getClass().getName().contains("String")) {
      try {
        synchronized (dateFormat) {
          return dateFormat.parse(date.toString());
        }
      } catch (ParseException ioe) {
        log.warn("Error parsing timestamp", ioe);
      }
    }
    return new Date(0);
  }

  private float getPreference(Object value) {
    if (value != null) {
      if (value.getClass().getName().contains("String")) {
        preferenceIsString = true;
        return Float.parseFloat(value.toString());
      } else {
        preferenceIsString = false;
        return Double.valueOf(value.toString()).floatValue();
      }
    } else {
      return 0.5f;
    }
  }

  private String getID(Object id, boolean isUser) {
    if (id.getClass().getName().contains("ObjectId")) {
      if (isUser) {
        userIsObject = true;
      } else {
        itemIsObject = true;
      }
      return ((ObjectId) id).toHexString();
    } else {
      return id.toString();
    }
  }

  private void checkData(String userID,
                         Iterable<List<String>> items,
                         boolean add) throws NoSuchUserException, NoSuchItemException {
    Preconditions.checkNotNull(userID);
    Preconditions.checkNotNull(items);
    Preconditions.checkArgument(!userID.isEmpty(), "userID is empty");
    for (List<String> item : items) {
      Preconditions.checkNotNull(item.get(0));
      Preconditions.checkArgument(!item.get(0).isEmpty(), "item is empty");
    }
    if (userIsObject && !ID_PATTERN.matcher(userID).matches()) {
      throw new IllegalArgumentException();
    }
    for (List<String> item : items) {
      if (itemIsObject && !ID_PATTERN.matcher(item.get(0)).matches()) {
        throw new IllegalArgumentException();
      }
    }
    if (!add && !isIDInModel(userID)) {
      throw new NoSuchUserException();
    }
    for (List<String> item : items) {
      if (!add && !isIDInModel(item.get(0))) {
        throw new NoSuchItemException();
      }
    }
  }

  /**
   * Cleanup mapping collection.
   */
  public void cleanupMappingCollection() {
    collectionMap.drop();
  }

  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    return delegate.getUserIDs();
  }

  @Override
  public PreferenceArray getPreferencesFromUser(long id) throws TasteException {
    return delegate.getPreferencesFromUser(id);
  }

  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    return delegate.getItemIDsFromUser(userID);
  }

  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    return delegate.getItemIDs();
  }

  @Override
  public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
    return delegate.getPreferencesForItem(itemID);
  }

  @Override
  public Float getPreferenceValue(long userID, long itemID) throws TasteException {
    return delegate.getPreferenceValue(userID, itemID);
  }

  @Override
  public Long getPreferenceTime(long userID, long itemID) throws TasteException {
    return delegate.getPreferenceTime(userID, itemID);
  }

  @Override
  public int getNumItems() throws TasteException {
    return delegate.getNumItems();
  }

  @Override
  public int getNumUsers() throws TasteException {
    return delegate.getNumUsers();
  }

  @Override
  public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
    return delegate.getNumUsersWithPreferenceFor(itemID);
  }

  @Override
  public int getNumUsersWithPreferenceFor(long itemID1, long itemID2) throws TasteException {
    return delegate.getNumUsersWithPreferenceFor(itemID1, itemID2);
  }

  @Override
  public void setPreference(long userID, long itemID, float value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePreference(long userID, long itemID) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPreferenceValues() {
    return delegate.hasPreferenceValues();
  }

  @Override
  public float getMaxPreference() {
    return delegate.getMaxPreference();
  }

  @Override
  public float getMinPreference() {
    return delegate.getMinPreference();
  }

  @Override
  public String toString() {
    return "MongoDBDataModel";
  }

}