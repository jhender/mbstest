package com.jhdev.mbstest.main.simplepin;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/** A custom Content Provider to do the database operations */
public class LocationsContentProvider extends ContentProvider{
 
    public static final String PROVIDER_NAME = "com.jhdev.mbstest.main.simplepin.locations";
 
    /** A uri to do operations on locations table. A content provider is identified by its uri */
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/locations" );
 
    /** Constant to identify the requested operation */
    private static final int LOCATIONS = 1;
    private static final int LOCATION_ID = 2;
 
    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "locations", LOCATIONS);
        uriMatcher.addURI(PROVIDER_NAME, "locations/#", LOCATION_ID);
    }    

	//instance variable for SQLiteDatabse
	private SQLiteDatabase mDB;	
	//table name, a constant
	private static final String DATABASE_TABLE = "locations";
	private static final String SUB_MAP_TABLE = "sub_map_table";     
	
	// Fields for locations table. keep this in order.
	public static final String FIELD_ROW_ID ="_id";
	public static final String FIELD_LAT = "lat";
	public static final String FIELD_LNG = "lng";
	public static final String FIELD_ZOOM = "zoom";
	public static final String FIELD_ADDRESS ="address";
	public static final String FIELD_SUB_MAP_ID = "sub_map_id";
	public static final String FIELD_TITLE = "title";
	
	// Fields for SUB_MAP_TABLE table
	public static final String FIELD_SUB_MAP_ROW_ID = "_id";
	public static final String FIELD_SUB_MAP = "sub_map";
	
    public class LocationsDB extends SQLiteOpenHelper{
    	
    	//Database Name
    	private static final String DBNAME = "locationmarkerrsqlite";
    	
    	//Version number of database
    	private static final int VERSION = 1; 


    	//constructor
    	public LocationsDB(Context context) {
    		super(context, DBNAME, null, VERSION);
    		//this.mDB = getWritableDatabase();
    	}
    	
    	/** This is a callback method, invoked when the method getReadableDatabase() / getWritableDatabase() is called
    	    * provided the database does not exists
    	    * */
    	    @Override
    	    public void onCreate(SQLiteDatabase db) {
    	        String create_table_locations =     "create table " + DATABASE_TABLE + " ( " +
    					                         FIELD_ROW_ID + " integer primary key autoincrement , " +
    					                         FIELD_LAT + " double , " +
    					                         FIELD_LNG + " double , " +
    					                         FIELD_ZOOM + " text , " +
    					                         FIELD_ADDRESS + " text , " +
    					                         FIELD_SUB_MAP_ID + " integer , " +
    					                         FIELD_TITLE + " text " +
    					                         " ) ";
    	 	        
    	        String create_table_submap = "create table " + SUB_MAP_TABLE + " ( " +
    	        							FIELD_SUB_MAP_ROW_ID + " integer primary key autoincrement , " +
    	        							FIELD_SUB_MAP + " text " +
    	        							" ) ";
    	        
    	        db.execSQL(create_table_locations);
    	        db.execSQL(create_table_submap);
    	        
    	        ContentValues first_sub_map = new ContentValues();
    	        final String first_sub_map_name = "Default Map";
    	        first_sub_map.put(FIELD_SUB_MAP, first_sub_map_name);
    	        db.insert(SUB_MAP_TABLE, null, first_sub_map);	        
    	    	Log.d("DB", "DB: query complete" + first_sub_map );

    	    }
    	           	 
    	    @Override
    	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {    	    	
    	    	//TODO ADD CODE
    	    }
    	    
//    	    // Return all locations joined with sub map name
//    	    public Cursor getAllLocations(){
//    	    	Cursor cursor;
//    	    	String query;
//    	    	query = "SELECT a._id, a.lat, a.lng, a.sub_map_id, a.zoom, a.address, a.title, b._id AS b_ID, b.sub_map FROM locations a" +
//    	    			" LEFT JOIN sub_map_table b ON a.sub_map_id=b._id";
//    	    	Log.d("DB", "DB: query complete" );
//    	    	//return mDB.rawQuery(query, new String[] {FIELD_ROW_ID,  FIELD_LAT , FIELD_LNG, FIELD_ZOOM, FIELD_ADDRESS, FIELD_SUB_MAP });
//    	    	cursor = mDB.rawQuery(query, null);
//    	    	return cursor;
//    	    }
    	    
    	    
    	}
    
    // END SqliteHelper code
    // Return back to ContentProvider code
    
 

    /** A callback method which is invoked when the content provider is starting up */
    @Override
    public boolean onCreate() {
        //mDB = new LocationsDB(getContext());
    	Context context = getContext();
    	LocationsDB dbHelper = new LocationsDB(context);
    	
    	mDB = dbHelper.getWritableDatabase();
        return (mDB == null)? false:true;
    }
    
    
    /** A callback method which is invoked when insert operation is requested on this content provider */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = mDB.insert(DATABASE_TABLE, null, values);
        Uri _uri=null;
        if(rowID>0){
            _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        }else {
            try {
                throw new SQLException("Failed to insert : " + uri);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return _uri;
    }
 
    @Override
    public int update(Uri uri, ContentValues values, String selection,
        String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /** A callback method which is invoked when delete operation is requested on this content provider */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //int cnt = mDB.delete(DATABASE_TABLE, null, null);
    	int count = 0;
    	
    	switch (uriMatcher.match(uri)){
    	case LOCATIONS:
    		count = mDB.delete(DATABASE_TABLE, selection, selectionArgs);
    		break;
    	case LOCATION_ID:
    		String id = uri.getPathSegments().get(1);
    		count = mDB.delete(DATABASE_TABLE, FIELD_ROW_ID + " = " + id, selectionArgs);
    		break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    	
    	getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

 
    /** A callback method which is invoked by default content uri */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    	
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(DATABASE_TABLE);
        
        switch (uriMatcher.match(uri)) {
        case LOCATIONS:
//        	builder = "SELECT a._id, a.lat, a.lng, a.sub_map_id, a.zoom, a.address, a.title, b._id AS b_ID, b.sub_map FROM locations a" +
//        			" LEFT JOIN sub_map_table b ON a.sub_map_id=b._id";;
        	break;
        case LOCATION_ID:
        	builder.appendWhere(FIELD_ROW_ID + " = " + uri.getLastPathSegment());
        	break;
        default:
        	throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == ""){
        	sortOrder = FIELD_ROW_ID;
        }
        
        Cursor c = builder.query(mDB, projection, selection, selectionArgs,
        		null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;       
    }
    
	// return all sub map names from table sub_map_table. not being used.
    public Cursor getAllSubMaps(){
    	return mDB.query(SUB_MAP_TABLE, new String[] { "sub_map" }, null, null, null, null, null);
    }
    
    // Insert new submap
    public boolean insertSubMap(ContentValues contentValues){
    	mDB.insert(SUB_MAP_TABLE, null, contentValues);
    	return true;
    }
 
    @Override
    public String getType(Uri uri) {
        return null;
    }
    
    
}