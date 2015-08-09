package org.orange.studentinformationdatabase;

import org.orange.parser.entity.Course;
import org.orange.parser.entity.Course.TimeAndAddress;
import org.orange.parser.entity.Post;
import org.orange.parser.util.BitOperate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据库名为：studentInf.db，有三张表，其中courseInf1和courseInf2是相关连的表，存储课程信息及成绩信息。表post存储通知信息。
 *
 * @author Zhou Peican
 */
public class StudentInfDBAdapter {

    private static final String DATABASE_NAME = "studentInf.db";

    private static final String DATABASE_COURSE_TABLE1 = "courseInf1";

    private static final String DATABASE_COURSE_TABLE2 = "courseInf2";

    /** Entity-attribute-value model table */
    public static final String DATABASE_EAV_TABLE = "entity_attribute_value";

    static final String DATABASE_POST_TABLE = "post";

    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    private final Context context;

    private StudentInfDBOpenHelper dbHelper;

    /**
     * 构造方法 建立数据库
     *
     * @param theContext 系统参数 “上下文”
     */
    public StudentInfDBAdapter(Context theContext) {
        context = theContext;
        dbHelper = new StudentInfDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String KEY_ID = "_id";

    public static final String KEY_CODE = "code";

    public static final String KEY_NAME = "name";

    public static final String KEY_TEACHERS = "teacrhers";

    public static final String KEY_CREDIT = "credit";

    public static final String KEY_CLASS_NUMBER = "class_number";

    public static final String KEY_TEACHING_MATERIAL = "teachingmaterial";

    public static final String KEY_YEAR = "year";

    public static final String KEY_SEMESTER = "semester";

    public static final String KEY_TEST_SCORE = "test_score";

    public static final String KEY_TOTAL_SCORE = "total_score";

    public static final String KEY_KIND = "kind";

    public static final String KEY_NOTE = "note";

    public static final String KEY_USER_NAME = "user_name";


    public static final String KEY_LINK = "link";

    public static final String KEY_VICEID = "viceid";

    public static final String KEY_WEEK = "week";

    public static final String KEY_DAY = "day";

    public static final String KEY_PERIOD = "period";

    public static final String KEY_ADDRESS = "address";


    public static final String KEY_POST_ID = Contract.Posts._ID;

    public static final String KEY_SOURCE = "source";

    public static final String KEY_CATEGORY = "category";

    public static final String KEY_TITLE = "title";

    public static final String KEY_URL = "url";

    public static final String KEY_AUTHOR = "author";

    public static final String KEY_DATE = "date";

    public static final String KEY_MAINBODY = "mainbody";

    /** 用于保存通知的参数化SQL语句 */
    private static final String SAVE_TO_POST_SQL = "INSERT OR REPLACE INTO " + DATABASE_POST_TABLE +
            "(" + KEY_POST_ID + ", " + KEY_SOURCE + ", " + KEY_CATEGORY + ", " + KEY_TITLE + ", "
            + KEY_URL + ", " + KEY_AUTHOR + ", " + KEY_DATE + ", " + KEY_MAINBODY +
            ") VALUES(?, ?, ?, ?, ?, ?, ?, ?);";

    // Columns for DATABASE_EAV_TABLE

    /** {@link #DATABASE_EAV_TABLE}的实体字段名 */
    public static final String KEY_ENTITY = "entity";

    /** {@link #DATABASE_EAV_TABLE}的属性字段名 */
    public static final String KEY_ATTRIBUTE = "attribute";

    /** {@link #DATABASE_EAV_TABLE}的值字段名 */
    public static final String KEY_VALUE = "value";

    /** 个人信息的实体（{@link #KEY_ENTITY}）名 */
    public static final String ENTITY_PERSONAL_INFORMATION = "PersonalInformation";

    /**
     * 参数1、2、3分别为{@link StudentInfDBAdapter#KEY_ENTITY ENTITY}、
     * {@link StudentInfDBAdapter#KEY_ATTRIBUTE ATTRIBUTE}、{@link StudentInfDBAdapter#KEY_VALUE
     * VALUE}
     */
    private static final String SAVE_TO_EAV_SQL = "INSERT OR REPLACE INTO "
            + StudentInfDBAdapter.DATABASE_EAV_TABLE +
            "(" + StudentInfDBAdapter.KEY_ENTITY + ", " + StudentInfDBAdapter.KEY_ATTRIBUTE + ", "
            + StudentInfDBAdapter.KEY_VALUE + ") VALUES(?, ?, ?)";

    // Select entity, attribute, value From entity_attribute_value Where entity IN (Select attribute From entity_attribute_value Where entity = ?);
    private static final String QUERY_SQL = String.format
            ("SELECT %2$s, %3$s, %4$s FROM %1$s WHERE %2$s IN (SELECT %3$s FROM %1$s WHERE %2$s = ?);",
                    StudentInfDBAdapter.DATABASE_EAV_TABLE, //%1$s
                    StudentInfDBAdapter.KEY_ENTITY, StudentInfDBAdapter.KEY_ATTRIBUTE,
                    StudentInfDBAdapter.KEY_VALUE);

    /*
     *  内部类，构建数据库用的，生成courseInf1、courseInf2和post表。
     */
    static class StudentInfDBOpenHelper extends SQLiteOpenHelper {

        public StudentInfDBOpenHelper(Context context, String name, CursorFactory factory,
                int version) {
            super(context, name, factory, version);
        }

        public StudentInfDBOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private static final String COURSE_TABLE1_CREATE = "create table " + DATABASE_COURSE_TABLE1
                + "(" + KEY_ID + " integer primary key,"
                + KEY_CODE + " character(7) unique," + KEY_NAME + " varchar(25)," + KEY_TEACHERS
                + " varchar(25)," + KEY_CREDIT + " tinyint," + KEY_CLASS_NUMBER + " varchar(5),"
                + KEY_TEACHING_MATERIAL + " varchar(15)," + KEY_YEAR + " integer," + KEY_SEMESTER
                + " INTEGER," + KEY_TEST_SCORE + " REAL,"
                + KEY_TOTAL_SCORE + " REAL," + KEY_KIND + " varchar(5)," + KEY_NOTE
                + " varchar(30)," + KEY_USER_NAME + " varchar(8));";

        private static final String COURSE_TABLE2_CREATE = "create table " + DATABASE_COURSE_TABLE2
                + "(" + KEY_LINK + " integer," + KEY_VICEID + " varchar(5) unique,"
                + KEY_WEEK + " integer," + KEY_DAY + " integer," + KEY_PERIOD + " integer,"
                + KEY_ADDRESS + " varchar(5));";

        private static final String POST_TABLE_CREATE = "create table " + DATABASE_POST_TABLE + "("
                + KEY_POST_ID + " integer primary key,"
                + KEY_SOURCE + " integer," + KEY_CATEGORY + " varchar(35)," + KEY_TITLE
                + " varchar(35)," + KEY_URL + " varchar(60)," + KEY_AUTHOR + " varchar(15),"
                + KEY_DATE + " integer," + KEY_MAINBODY + " text);";

        private static final String EAV_TABLE_CREATE = "CREATE TABLE " + DATABASE_EAV_TABLE + "("
                + BaseColumns._ID + " INTEGER PRIMARY KEY ASC, " + KEY_ENTITY
                + " VARCHAR NOT NULL, "
                + KEY_ATTRIBUTE + " VARCHAR NOT NULL, " + KEY_VALUE
                + " VARCHAR, CONSTRAINT unique_eva UNIQUE(" + KEY_ENTITY + ", " + KEY_ATTRIBUTE
                + "));";


        public void onCreate(SQLiteDatabase theDB) {
            theDB.execSQL(COURSE_TABLE1_CREATE);
            theDB.execSQL(COURSE_TABLE2_CREATE);
            theDB.execSQL(POST_TABLE_CREATE);
            theDB.execSQL(
                    "CREATE INDEX post_index ON " + DATABASE_POST_TABLE + "(" + KEY_DATE + ");");
            theDB.execSQL(EAV_TABLE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase theDB, int theOldVersion, int theNewVersion) {
            Log.w("StudentInfDBAdapter",
                    "Upgrading from version " + theOldVersion + " to " + theNewVersion
                            + ", which will destroy all data"
            );

            theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_COURSE_TABLE1);
            theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_COURSE_TABLE2);
            theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_POST_TABLE);
            theDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_EAV_TABLE);

            onCreate(theDB);
        }
    }

    /**
     * getWritableDatabase()创建一个可读写的数据库。
     */
    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = dbHelper.getReadableDatabase();
        }
    }

    /**
     * Returns true if the database is currently open.
     *
     * @return True if the database is currently open (has not been closed).
     */
    public boolean isOpen() {
        return (db != null && db.isOpen());
    }

    public void close() {
        if (isOpen()) {
            db.close();
        }
    }

    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return dbHelper;
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    /**
     * 持久化二维映射到数据库中
     *
     * @param map   要持久化的二位映射
     * @param mapId 次映射对象的ID，提取时使用
     * @return 保存过程中影响数据库的行数（映射的条目（{@link Map.Entry}）数，包括外层映射条目）
     * @see #retrieveTwodimensionalMap(String)
     */
    public long saveTwodimensionalMap(Map<String, Map<String, String>> map, String mapId) {
        if (map == null || mapId == null || mapId.length() == 0) {
            throw new IllegalArgumentException(
                    "map == null || mapId == null || mapId.length() == 0");
        }
        long counter = 0;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        SQLiteStatement statement = database.compileStatement(SAVE_TO_EAV_SQL); // TODO SQLException
        database.beginTransaction();
        try {
            for (Map.Entry<String, Map<String, String>> group : map.entrySet()) {
                statement.clearBindings();
                String groupName = group.getKey();
                statement.bindString(1, mapId);
                statement.bindString(2, groupName);
                if (statement.executeInsert() != -1) //TODO SQLException
                {
                    counter++;
                }
                for (Map.Entry<String, String> keyValue : group.getValue().entrySet()) {
                    statement.bindString(1, groupName);
                    statement.bindString(2, keyValue.getKey());
                    if(keyValue.getValue() != null) {
                        statement.bindString(3, keyValue.getValue());
                    } else {
                        statement.bindNull(3);
                    }
                    if (statement.executeInsert() != -1) //TODO SQLException
                    {
                        counter++;
                    }
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            if (database != db) {
                database.close();
            }
        }
        return counter;
    }

    /**
     * 反持久化二维映射（提取持久化的二维映射）
     *
     * @param mapId 要提取的映射对象的ID
     * @return 反持久化后的映射对象
     * @see #saveTwodimensionalMap(Map, String)
     */
    public Map<String, Map<String, String>> retrieveTwodimensionalMap(String mapId) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Cursor cursor = db.rawQuery(QUERY_SQL, new String[]{mapId});
        while (cursor.moveToNext()) {
            String groupName = cursor.getString(0);
            if (!result.containsKey(groupName)) {
                result.put(groupName, new HashMap<String, String>());
            }
            result.get(groupName).put(cursor.getString(1), cursor.getString(2));
        }
        cursor.close();
        return result;
    }

    /**
     * 一次性插入多门课程及每门课程成绩的初始化（成绩与课程在同一张表中），之所以成绩要初始化是因为读取课程时并没有读取成绩，
     *
     * @param theCourseInf 类型为 List<Course>
     */
    private void insertArrayCoursesToCourseInf1(List<Course> theCourseInf, String theUserName) {
        Cursor cursor, cursor1;
        ContentValues newCourseInfValues = new ContentValues();

        cursor = db.query(DATABASE_COURSE_TABLE1, null, KEY_YEAR + "=" + 0, null, null, null, null);

        for (Course aCourse : theCourseInf) {
            newCourseInfValues.put(KEY_CODE, aCourse.getCode());
            newCourseInfValues.put(KEY_NAME, aCourse.getName());
            newCourseInfValues.put(KEY_TEACHERS, aCourse.getTeacherString());
            newCourseInfValues.put(KEY_CREDIT, aCourse.getCredit());
            newCourseInfValues.put(KEY_CLASS_NUMBER, aCourse.getClassNumber());
            newCourseInfValues.put(KEY_TEACHING_MATERIAL, aCourse.getTeachingMaterial());
            newCourseInfValues.put(KEY_YEAR, aCourse.getYear());
            if (aCourse.getSemester() != null) {
                newCourseInfValues.put(KEY_SEMESTER, aCourse.getSemester());
            }
            newCourseInfValues.put(KEY_TEST_SCORE, aCourse.getTestScore());
            newCourseInfValues.put(KEY_TOTAL_SCORE, aCourse.getTotalScore());
            newCourseInfValues.put(KEY_KIND, aCourse.getKind());
            newCourseInfValues.put(KEY_NOTE, aCourse.getNote());
            newCourseInfValues.put(KEY_USER_NAME, theUserName);
            //theCourseInf为ArrayList对象，get(i)顺序找到其中的一门课程。getCode()等方法得到相应实例变量的值。

            //判断数据库课程表中是否已经有要查入的课程，如果已经有就不会再次插入
            cursor1 = db
                    .query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + aCourse.getCode() + "'",
                            null, null, null, null);
            if (cursor1.getCount() == 0) {
                db.insert(DATABASE_COURSE_TABLE1, null, newCourseInfValues);
            } else {
                newCourseInfValues.clear();
            }
        }

    }

    /**
     * 课程的时间地点是另一张表，这是对时间地点的插入操作。courseInf2为时间地点的存储与courseInf1相关联。
     *
     * @param theCourseInf List<Course>类型
     */
    private void insertArrayCoursesToCourseInf2(List<Course> theCourseInf) {
        Cursor theCursor;
        String theViceId;
        ContentValues newCourseInfTAValues = new ContentValues();
        int counter = 0;
        for (Course aCourse : theCourseInf) {
            Cursor cursor = db
                    .query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + aCourse.getCode() + "'",
                            null, null, null, null);
            cursor.moveToFirst();
            //这里对courseInf1表的查询是为了获得cuorseInf1表的id字段值，用来存储到courseInf2中的link字段。
            counter = 0;
            for (Course.TimeAndAddress aTimeAndAddress : aCourse.getTimeAndAddress()) {
                newCourseInfTAValues.put(KEY_LINK, cursor.getInt(0));
                //link列是courseInf1和courseInf2相‘连接’的字段以执行相应的操作，所以link列须和_id列值相等。cursor.getInt(0)是得到id字段值。
                theViceId = Integer.toString(cursor.getInt(0)) + Integer.toString(counter++);
                newCourseInfTAValues.put(KEY_VICEID, theViceId);
                //一门课有多个TimeAndAddress与之相对应，viceid列是为了对其中一个TimeAndAddress进行操作。例如当link列为1时viceid就为11、12、、、等
                //当然link的1是整数型的而viceid的11、12为字符串类型的。
                newCourseInfTAValues.put(KEY_WEEK, aTimeAndAddress.getWeek());
                newCourseInfTAValues.put(KEY_DAY, aTimeAndAddress.getDay());
                newCourseInfTAValues.put(KEY_PERIOD, aTimeAndAddress.getPeriod());
                newCourseInfTAValues.put(KEY_ADDRESS, aTimeAndAddress.getAddress());

                //判断数据库课程表中是否已经有要查入的课程，如果已经有就不会再次插入
                theCursor = db
                        .query(DATABASE_COURSE_TABLE2, null, KEY_VICEID + "= '" + theViceId + "'",
                                null, null, null, null);
                if (theCursor.getCount() == 0) {
                    db.insert(DATABASE_COURSE_TABLE2, null, newCourseInfTAValues);
                } else {
                    newCourseInfTAValues.clear();
                }
            }
            cursor.close();
        }
    }

    /**
     * 先判断数据库课程的记录是否为当前用户的，如果不是就清空课程的记录，建立当前用户的课程信息记录。是当前用户的就继续操作
     *
     * @param theCourseInf List<Course>类型
     * @param theUserName  String类型
     */
    public void autoInsertArrayCoursesInf(List<Course> theCourseInf, String theUserName) {
        if (theCourseInf == null || theCourseInf.isEmpty()) {
            return;
        }
        Cursor cursor1 = db
                .query(DATABASE_COURSE_TABLE1, null, KEY_USER_NAME + "= '" + theUserName + "'",
                        null, null, null, null);
        if (cursor1.getCount() != 0) {
            insertArrayCoursesToCourseInf1(theCourseInf, theUserName);
            insertArrayCoursesToCourseInf2(theCourseInf);
        } else {
            db.delete(DATABASE_COURSE_TABLE1, null, null);
            db.delete(DATABASE_COURSE_TABLE2, null, null);

            String code = theCourseInf.get(0).getCode();
            Cursor cursor2 = db
                    .query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + code + "'", null, null,
                            null, null);
            if (cursor2.getCount() == 0) {
                insertArrayCoursesToCourseInf1(theCourseInf, theUserName);
                insertArrayCoursesToCourseInf2(theCourseInf);
            }
            cursor2.close();
        }
        cursor1.close();
    }

    /**
     * 这个课程插入的方法是一次插入一门课程及相关成绩的初始化，
     *
     * @param theCourseInf 类型为Course类型
     */
    private void insertCourseToCourseInf1(Course theCourseInf, String theUserName) {
        ContentValues newCourseInfValues = new ContentValues();

        newCourseInfValues.put(KEY_CODE, theCourseInf.getCode());
        newCourseInfValues.put(KEY_NAME, theCourseInf.getName());
        newCourseInfValues.put(KEY_TEACHERS, theCourseInf.getTeacherString());
        newCourseInfValues.put(KEY_CREDIT, theCourseInf.getCredit());
        newCourseInfValues.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
        newCourseInfValues.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
        newCourseInfValues.put(KEY_YEAR, theCourseInf.getYear());
        if (theCourseInf.getSemester() != null) {
            newCourseInfValues.put(KEY_SEMESTER, theCourseInf.getSemester());
        }
        newCourseInfValues.put(KEY_TEST_SCORE, theCourseInf.getTestScore());
        newCourseInfValues.put(KEY_TOTAL_SCORE, theCourseInf.getTotalScore());
        newCourseInfValues.put(KEY_KIND, theCourseInf.getKind());
        newCourseInfValues.put(KEY_NOTE, theCourseInf.getNote());
        newCourseInfValues.put(KEY_USER_NAME, theUserName);

        theCourseInf.setId(db.insert(DATABASE_COURSE_TABLE1, null, newCourseInfValues));
    }

    /**
     * 对一门课程的时间和地点的插入操作。
     */
    private void insertCourseToCourseInf2(Course theCourseInf) {
        ContentValues newCourseInfTAValues = new ContentValues();
        Cursor cursor = db
                .query(DATABASE_COURSE_TABLE1, null, KEY_ID + "=" + theCourseInf.getId(), null,
                        null, null, null);

        System.err.println("count: " + cursor.getCount() + "\nmovetofirst" + cursor.moveToFirst());

        for (int i = 0; i < theCourseInf.getTimeAndAddress().size(); i++) {
            newCourseInfTAValues.put(KEY_LINK, cursor.getInt(0));
            newCourseInfTAValues
                    .put(KEY_VICEID, Integer.toString(cursor.getInt(0)) + Integer.toString(i));
            newCourseInfTAValues.put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(i).getWeek());
            newCourseInfTAValues.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(i).getDay());
            newCourseInfTAValues
                    .put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(i).getPeriod());
            newCourseInfTAValues
                    .put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());

            db.insert(DATABASE_COURSE_TABLE2, null, newCourseInfTAValues);
        }
    }

    /**
     * 判断数据库课程表中是否已经有要查入的课程，如果已经有就不会再次插入，当然如果 没有就会掉用insertCourseToCourseInf1方法和insertCourseToCourseInf2。
     * insertCourseInf:这个课程插入的方法是一次插入一门课程及相关成绩的初始化，
     *
     * @param theCourseInf Course类型
     * @param theUserName  String类型
     */
    public void autoInsertCourseInf(Course theCourseInf, String theUserName) {
        if (theCourseInf == null) {
            return;
        }
        String code = theCourseInf.getCode();
        Cursor cursor = db
                .query(DATABASE_COURSE_TABLE1, null, KEY_CODE + "= '" + code + "'", null, null,
                        null, null);
        if (cursor.getCount() == 0) {
            insertCourseToCourseInf1(theCourseInf, theUserName);
            insertCourseToCourseInf2(theCourseInf);
        }
    }

    /**
     * 持久化通知。使用{@code INSERT OR REPLACE INTO}语句保存。
     *
     * @param posts 要持久化的通知
     * @return 保存过程中影响数据库的行数（通知的条数）
     */
    public long savePosts(Collection<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return 0;
        }
        long counter = 0;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        SQLiteStatement statement = database
                .compileStatement(SAVE_TO_POST_SQL); // TODO SQLException
        database.beginTransaction();
        try {
            for (Post post : posts) {
                statement.clearBindings();
                if (post.getId() != null) {
                    statement.bindLong(1, post.getId());
                }
                statement.bindLong(2, post.getSource());
                bindStringIfNotNull(statement, 3, post.getCategory());
                bindStringIfNotNull(statement, 4, post.getTitle());
                bindStringIfNotNull(statement, 5, post.getUrl());
                bindStringIfNotNull(statement, 6, post.getAuthor());
                statement.bindLong(7, post.getDate().getTime());
                bindStringIfNotNull(statement, 8, post.getMainBody());
                if (statement.executeInsert() != -1) //TODO SQLException
                {
                    counter++;
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            if (database != db) {
                database.close();
            }
        }
        return counter;
    }

    private static void bindStringIfNotNull(SQLiteStatement statement, int index, String value) {
        if (value != null) {
            statement.bindString(index, value);
        }
    }

    /**
     * 删除一门课程的所有信息，也就是一条记录。这调记录为courseInf1和courseInf2的相关的一门课程的信息，
     * 如courseInf1的_id为1的行进行删除时courseInf2的link为1的行也要进行删除。
     *
     * @param theCourseInf 类型为Course类型
     * @return boolean, 表示删除是否成功。<br />另，若theCourseInf为空，返回false。
     */
    public boolean deleteCourseInf(Course theCourseInf) {
        if (theCourseInf == null) {
            return false;
        }
        long rowIndex = theCourseInf.getId();
        return (db.delete(DATABASE_COURSE_TABLE1, KEY_ID + "=" + rowIndex, null) > 0)
                && (db.delete(DATABASE_COURSE_TABLE2, KEY_LINK + "=" + rowIndex, null) > 0);
    }


    /**
     * 删除表post中的一条记录，此删除方法是根据参数传递进来的一个Post对象，获得这条消息的标题，从而根据标题进行删除。
     *
     * @param thePostInf 类型为Post类型
     * @return boolean，表示删除是否已经成功。<br />另，若thePostInf为空，返回false。
     */
    public boolean deletePostInf(Post thePostInf) {
        if (thePostInf == null) {
            return false;
        }
        String theTitle = thePostInf.getTitle();
        return db.delete(DATABASE_POST_TABLE, KEY_TITLE + "= '" + theTitle + "'", null) > 0;
    }

    /**
     * 对课程的更新操作，从参数传递进来一门课程，再从数据库中找到这门课的记录，然后进行比较，比较结果不一样就对数据库中的这条记录相应的字段进行更改。
     *
     * @param theCourseInf 类型为Course类型
     */
    public void updateCourseInf(Course theCourseInf) {
        if (theCourseInf == null) {
            return;
        }
        ContentValues newCourseInfValues1 = new ContentValues();
        ContentValues newCourseInfValues2 = new ContentValues();
        ContentValues newCourseInfValues3 = new ContentValues();
        ContentValues newCourseInfValues4 = new ContentValues();

        //从传递进来的参数获得这门课程的记录的_id，进行查询，获得courseInf1和courseInf2中的这门课的记录。
        long rowIndex = theCourseInf.getId();
        Cursor cursor1 = db
                .query(DATABASE_COURSE_TABLE1, null, KEY_ID + "=" + rowIndex, null, null, null,
                        null);
        Cursor cursor2 = db
                .query(DATABASE_COURSE_TABLE2, null, KEY_LINK + "=" + rowIndex, null, null, null,
                        null);
        cursor1.moveToFirst();
        cursor2.moveToFirst();
        int count = cursor2.getCount();

        if (cursor1.getString(1)
                != null)//数据库里的字符串初始化值一般都为空，所以从数据库中获得字符串时要进行判断是否为空。为空是直接进行更新操作，不为空时进行数据库相应课程对应的字段的值与传递进来的课程相比较
        {
            if (!(cursor1.getString(1).equals(theCourseInf.getCode())))//判断数据库中与这门课传递进来的相应字段值是否相等
            {
                newCourseInfValues1.put(KEY_CODE, theCourseInf.getCode());
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            }
        } else {
            newCourseInfValues1.put(KEY_CODE, theCourseInf.getCode());
            db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
        }

        if (cursor1.getString(2) != null) {
            if (!(cursor1.getString(2).equals(theCourseInf.getName()))) {
                newCourseInfValues1.put(KEY_NAME, theCourseInf.getName());
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            }
        } else {
            newCourseInfValues1.put(KEY_NAME, theCourseInf.getName());
            db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
        }

        if (cursor1.getString(3) != null) {
            if (!(cursor1.getString(3).equals(theCourseInf.getTeacherString()))) {
                newCourseInfValues1.put(KEY_TEACHERS, theCourseInf.getTeacherString());
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            }
        } else {
            newCourseInfValues1.put(KEY_TEACHERS, theCourseInf.getTeacherString());
            db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
        }

        if (cursor1.getInt(4) != theCourseInf.getCredit()) {
            if (theCourseInf.getCredit() < 0) {
                newCourseInfValues1.put(KEY_CREDIT, 0);
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            } else {
                newCourseInfValues1.put(KEY_CREDIT, theCourseInf.getCredit());
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            }
        }

        if (cursor1.getString(5) != null) {
            if (!(cursor1.getString(5).equals(theCourseInf.getClassNumber()))) {
                newCourseInfValues1.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            }
        } else {
            newCourseInfValues1.put(KEY_CLASS_NUMBER, theCourseInf.getClassNumber());
            db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
        }

        if (cursor1.getString(6) != null) {
            if (!(cursor1.getString(6).equals(theCourseInf.getTeachingMaterial()))) {
                newCourseInfValues1.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
                db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex,
                        null);
            }
        } else {
            newCourseInfValues1.put(KEY_TEACHING_MATERIAL, theCourseInf.getTeachingMaterial());
            db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1, KEY_ID + "=" + rowIndex, null);
        }

        if (count
                != 0)//判断这门课的TimeAndAddress是否没有。如外教的时间和地点就没此时ArrayList对象的TimeAndAddress的size就为0，数据库中的courseInf2中就没了这条记录，
        {                          //当没这天记录时就进行插入操作，当有时就要进行比较是否一样，不一样就要进行更改。
            for (int i = 0; i < count; i++) {
                cursor2.moveToPosition(i);
                if (i >= theCourseInf.getTimeAndAddress().size()) {
                    newCourseInfValues2.put(KEY_WEEK, 0);
                    db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2,
                            KEY_VICEID + " = " + (String.valueOf(rowIndex) + (i)), null);
                    continue;
                }
                if (cursor2.getInt(2) != theCourseInf.getTimeAndAddress().get(i).getWeek()) {
                    newCourseInfValues2
                            .put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(i).getWeek());
                    db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2,
                            KEY_VICEID + "=" + (String.valueOf(rowIndex) + (i)), null);
                }

                if (cursor2.getInt(3) != theCourseInf.getTimeAndAddress().get(i).getDay()) {
                    newCourseInfValues2
                            .put(KEY_DAY, theCourseInf.getTimeAndAddress().get(i).getDay());
                    db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2,
                            KEY_VICEID + "=" + (String.valueOf(rowIndex) + (i)), null);
                }

                if (cursor2.getInt(4) != theCourseInf.getTimeAndAddress().get(i).getPeriod()) {
                    newCourseInfValues2
                            .put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(i).getPeriod());
                    db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2,
                            KEY_VICEID + "=" + (String.valueOf(rowIndex) + (i)), null);
                }

                if (cursor2.getString(5) != null) {
                    if (!(cursor2.getString(5)
                            .equals(theCourseInf.getTimeAndAddress().get(i).getAddress()))) {
                        newCourseInfValues2.put(KEY_ADDRESS,
                                theCourseInf.getTimeAndAddress().get(i).getAddress());
                        db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2,
                                KEY_VICEID + "=" + (String.valueOf(rowIndex) + (i)), null);
                    }
                } else {
                    newCourseInfValues2
                            .put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(i).getAddress());
                    db.update(DATABASE_COURSE_TABLE2, newCourseInfValues2,
                            KEY_VICEID + "=" + (String.valueOf(rowIndex) + (i)), null);
                }
            }

            //用于当修改课程时增加了时间地点，进行插入数据库中，count + j代表要插入的时间地点的下标
            if (theCourseInf.getTimeAndAddress().size() > count) {
                for (int j = 0; j < theCourseInf.getTimeAndAddress().size() - count; j++) {
                    newCourseInfValues3.put(KEY_LINK, rowIndex);
                    newCourseInfValues3.put(KEY_VICEID, String.valueOf(rowIndex) + (count + j));
                    newCourseInfValues3.put(KEY_WEEK,
                            theCourseInf.getTimeAndAddress().get(count + j).getWeek());
                    newCourseInfValues3
                            .put(KEY_DAY, theCourseInf.getTimeAndAddress().get(count + j).getDay());
                    newCourseInfValues3.put(KEY_PERIOD,
                            theCourseInf.getTimeAndAddress().get(count + j).getPeriod());
                    newCourseInfValues3.put(KEY_ADDRESS,
                            theCourseInf.getTimeAndAddress().get(count + j).getAddress());

                    db.insert(DATABASE_COURSE_TABLE2, null, newCourseInfValues3);
                }
            }
        } else {
            //这是当没有时间地点的课程的时间地点的插入
            for (int j = 0; j < theCourseInf.getTimeAndAddress().size(); j++) {
                newCourseInfValues4.put(KEY_LINK, rowIndex);
                newCourseInfValues4.put(KEY_VICEID, (String.valueOf(rowIndex) + (j)));
                newCourseInfValues4
                        .put(KEY_WEEK, theCourseInf.getTimeAndAddress().get(j).getWeek());
                newCourseInfValues4.put(KEY_DAY, theCourseInf.getTimeAndAddress().get(j).getDay());
                newCourseInfValues4
                        .put(KEY_PERIOD, theCourseInf.getTimeAndAddress().get(j).getPeriod());
                newCourseInfValues4
                        .put(KEY_ADDRESS, theCourseInf.getTimeAndAddress().get(j).getAddress());

                db.insert(DATABASE_COURSE_TABLE2, null, newCourseInfValues4);
            }
        }

        //这里是做检查使用，当检查到week等于0时表明这个时间地点已经是出去的，这里就会把它从数据库中删除
        cursor2 = db
                .query(DATABASE_COURSE_TABLE2, null, KEY_LINK + "=" + rowIndex, null, null, null,
                        null);
        cursor2.moveToFirst();
        for (int i = 0; i < cursor2.getCount(); i++) {
            cursor2.moveToPosition(i);
            if (cursor2.getInt(2) == 0) {
                db.delete(DATABASE_COURSE_TABLE2,
                        KEY_VICEID + "=" + (String.valueOf(rowIndex) + (i)), null);
            }
        }
    }

    /**
     * 对单门的成绩进行更新
     */
    public void updateScoreInf(Course theScoreInf) {
        List<Course> courses = new ArrayList<Course>();
        courses.add(theScoreInf.clone());
        updateScoreInf(courses);
    }

    /**
     * 成绩只有更新，因为在课程插入时就已经对成绩进行了初始化。
     *
     * @param theScoreInf 类型为List<Course>,当出一门课程成绩时生成一个成员的ArrrayList类型，就可以调用此方法进行更新。
     */
    public void updateScoreInf(List<Course> theScoreInf) {
        if (theScoreInf == null || theScoreInf.isEmpty()) {
            return;
        }
        ContentValues newCourseInfValues1 = new ContentValues();

        for (Course aScore : theScoreInf) {
            Cursor cursor1 = db
                    .query(DATABASE_COURSE_TABLE1, null, KEY_CODE + " = '" + aScore.getCode() + "'",
                            null, null, null, null);
            //getCode()得到课程代码实现成绩插入到相应的课程中。getCode()是字符串所以两边要加单引号。
            if (cursor1.getCount() == 0) {
                continue;
            }
            cursor1.moveToFirst();
            if (cursor1.getInt(7) != aScore.getYear())//判断数据库的内容和ArrayList的成绩对象的相关字段是否想等
            {
                if (aScore.getYear() <= 1900) {
                    newCourseInfValues1.put(KEY_YEAR, 0);
                } else {
                    newCourseInfValues1.put(KEY_YEAR, aScore.getYear());
                }
            }
            if (aScore.getSemester() != null && aScore.getSemester().byteValue() != cursor1.getInt(
                    8)) {
                newCourseInfValues1.put(KEY_SEMESTER, aScore.getSemester());
            }

            if (aScore.getTestScore() != null && cursor1.getDouble(9) != aScore.getTestScore()) {
                if (aScore.getTestScore() < 0) {
                    newCourseInfValues1.put(KEY_TEST_SCORE, Double.NaN);
                } else {
                    newCourseInfValues1.put(KEY_TEST_SCORE, aScore.getTestScore());
                }
            }

            if (aScore.getTotalScore() != null && cursor1.getDouble(10) != aScore.getTotalScore()) {
                if (aScore.getTotalScore() < 0) {
                    newCourseInfValues1.put(KEY_TOTAL_SCORE, Double.NaN);
                } else {
                    newCourseInfValues1.put(KEY_TOTAL_SCORE, aScore.getTotalScore());
                }
            }

            if (cursor1.getString(11)
                    != null)//成绩这一块已经在课程的表courseInf1建立时就已经初始化了，初始化时字符串是空的，所以这里要判断是否为空
            {
                if (!(cursor1.getString(11).equals(aScore.getKind()))) {
                    newCourseInfValues1.put(KEY_KIND, aScore.getKind());
                }
            } else {
                newCourseInfValues1.put(KEY_KIND, aScore.getKind());
            }

            if (cursor1.getString(12) != null) {
                if (!(cursor1.getString(12).equals(aScore.getNote()))) {
                    newCourseInfValues1.put(KEY_NOTE, aScore.getNote());
                }
            } else {
                newCourseInfValues1.put(KEY_NOTE, aScore.getNote());
            }
            db.update(DATABASE_COURSE_TABLE1, newCourseInfValues1,
                    KEY_CODE + " = '" + aScore.getCode() + "'", null);
            newCourseInfValues1.clear();
            cursor1.close();
        }
    }

    /**
     * 取得指定用户的所有课程信息（包括成绩、时间等）
     *
     * @param theUserName 用户名
     * @return see {@link #getAllCoursesFromDB(String, String[])}
     * @see #getAllCoursesFromDB(String, String[])
     */
    public Map<Integer, Map<Integer, List<Course>>> getAllCoursesFromDB(String theUserName) {
        return getAllCoursesFromDB(KEY_USER_NAME + " = ?", new String[]{theUserName});
    }

    /**
     * 取得满足制定条件的所有课程信息（包括成绩、时间等）
     *
     * @return 分别以学年、学期为{@code Key}的课程列表二维映射，学年学期不明时{@code Key}为null
     * <p>示例：{@code result.get(2013).get(1)}</p>
     * @see #getCoursesFromDB(String, String[], String)
     */
    public Map<Integer, Map<Integer, List<Course>>> getAllCoursesFromDB(String selection,
            String[] selectionArgs) {
        Map<Integer, Map<Integer, List<Course>>> result = new HashMap<>();
        for (Course course : getCoursesFromDB(selection, selectionArgs, null)) {
            Integer year = (int) course.getYear();
            Integer semester = course.getSemester() != null ? (int) course.getSemester() : null;
            if (!result.containsKey(year)) {
                result.put(year, new HashMap<Integer, List<Course>>());
            }
            if (!result.get(year).containsKey(semester)) {
                result.get(year).put(semester, new LinkedList<Course>());
            }
            result.get(year).get(semester).add(course);
        }
        return result;
    }

    /**
     * 取得指定用户的所有课程信息（包括成绩、时间等）
     *
     * @param userName 用户名
     * @see #getCoursesFromDB(String, String[], String)
     */
    public List<Course> getCoursesFromDB(String selection, String[] selectionArgs, String orderBy,
            String userName) {
        if (userName == null) {
            throw new SQLException("ueserName shouldn't be null"); //TODO 改善NullPointerException
        }
        selection = String.format("(%s) AND %s = ?", selection, KEY_USER_NAME);
        String[] newSelectionArgs = null;
        if (selectionArgs == null) {
            newSelectionArgs = new String[]{userName};
        } else {
            newSelectionArgs = new String[selectionArgs.length + 1];
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);
            newSelectionArgs[newSelectionArgs.length - 1] = userName;
        }
        return getCoursesFromDB(selection, newSelectionArgs, orderBy);
    }

    /**
     * 取得满足制定条件的所有课程信息（包括成绩、时间等）
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause
     *                      (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @return 指定顺序的课程列表
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String)
     */
    public List<Course> getCoursesFromDB(String selection, String[] selectionArgs, String orderBy) {
        /** 用于查找TimeAndAddresses */
        String taaSQL = SQLiteQueryBuilder
                .buildQueryString(true, DATABASE_COURSE_TABLE1, new String[]{KEY_ID}, selection,
                        null, null, null, null);
        taaSQL = SQLiteQueryBuilder.buildQueryString(true, DATABASE_COURSE_TABLE2, null,
                KEY_LINK + " IN (" + taaSQL + ")", null, null, null, null);
        Map<Long, Map<String, TimeAndAddress>> taas = null;
        List<Course> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            db.beginTransaction();
            try {
                cursor = db
                        .query(DATABASE_COURSE_TABLE1, null, selection, selectionArgs, null, null,
                                orderBy);
                taas = getTimeAndAddress(taaSQL, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            while (cursor.moveToNext()) {
                Course course = covertToCourse(cursor);
                Map<String, TimeAndAddress> courseTaas = taas.get((long) course.getId());
                if (courseTaas != null) {
                    for (TimeAndAddress taa : courseTaas.values()) {
                        course.addTimeAndAddress(taa);
                    }
                }
                result.add(course);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private Map<Long, Map<String, TimeAndAddress>> getTimeAndAddress(String sql,
            String[] selectionArgs) {
        Map<Long, Map<String, TimeAndAddress>> result = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LINK));
                String viceid = cursor.getString(cursor.getColumnIndexOrThrow(KEY_VICEID));
                if (!result.containsKey(courseId)) {
                    result.put(courseId, new HashMap<String, TimeAndAddress>());
                }
                result.get(courseId).put(viceid, convertToTimeAndAddress(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private static Course covertToCourse(Cursor cursor) {
        Course result = new Course();
        boolean haveValue = false;
        for (int i = 0; i < cursor.getColumnCount(); i++) {
                switch (cursor.getColumnName(i)) {
                    case KEY_USER_NAME:
                        continue;        // skip these
                    case KEY_ID:
                    result.setId(cursor.getLong(i));
                        break;
                    case KEY_CODE:
                        result.setCode(cursor.getString(i));
                        break;
                    case KEY_NAME:
                        result.setName(cursor.getString(i));
                        break;
                    case KEY_TEACHERS:
                    result.addTeachers(cursor.getString(i));
                        break;
                    case KEY_CREDIT:
                        result.setCredit(cursor.getInt(i));
                        break;
                    case KEY_CLASS_NUMBER:
                        result.setClassNumber(cursor.getString(i));
                        break;
                    case KEY_TEACHING_MATERIAL:
                        result.setTeachingMaterial(cursor.getString(i));
                        break;
                    case KEY_YEAR:
                        result.setYear(cursor.getInt(i));
                        break;
                    case KEY_SEMESTER:
                        if (!cursor.isNull(i)) {
                        result.setSemester(cursor.getInt(i));
                        }
                        break;
                    case KEY_TEST_SCORE:
                    result.setTestScore(cursor.getDouble(i));
                        break;
                    case KEY_TOTAL_SCORE:
                    result.setTotalScore(cursor.getDouble(i));
                        break;
                    case KEY_KIND:
                        result.setKind(cursor.getString(i));
                        break;
                    case KEY_NOTE:
                        result.setNote(cursor.getString(i));
                        break;
                    default:
                        throw new AssertionError("Unknown Column: " + cursor.getColumnName(i));
                }
                if (!haveValue) {
                    haveValue = true;
                }
        }
        if (!haveValue) {
            throw new AssertionError("Illegal Columns: None column who have meaningful value");
        }
        return result;
    }

    private static TimeAndAddress convertToTimeAndAddress(Cursor cursor) {
        TimeAndAddress result = new TimeAndAddress();
        boolean haveValue = false;
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            try {
                switch (cursor.getColumnName(i)) {
                    case KEY_LINK:
                    case KEY_VICEID:
                        continue;        // skip these
                    case KEY_WEEK:
                        result.setWeek(cursor.getInt(i));
                        break;
                    case KEY_DAY:
                        result.setDay(cursor.getInt(i));
                        break;
                    case KEY_PERIOD:
                        result.setPeriod(cursor.getInt(i));
                        break;
                    case KEY_ADDRESS:
                        result.setAddress(cursor.getString(i));
                        break;
                    default:
                        throw new AssertionError("Unknown Column: " + cursor.getColumnName(i));
                }
                if (!haveValue) {
                    haveValue = true;
                }
            } catch (BitOperate.BitOperateException e) {
                e.printStackTrace();
            }
        }
        if (!haveValue) {
            throw new AssertionError("Illegal Columns: None column who have meaningful value");
        }
        return result;
    }

    /**
     * 在where条件下，从post表中读取所有的通知信息。
     *
     * @param where。order.limit这三个参数和mysql的用法一样，调用时的参数用法如：StudentInfDBAdapter.KEY_TITLE + "=" + xxxx,
     *                                                                                  StudentInfDBAdapter.KEY_DATE
     *                                                                                  + " DESC",
     *                                                                                  "2".
     * @return ArrayList<Post>
     */
    public ArrayList<Post> getPostsFromDB(String where, String order, String limit)
            throws SQLException {
        ArrayList<Post> posts = new ArrayList<Post>();
        Post post = new Post();
        Cursor cursor = db.query(DATABASE_POST_TABLE, null, where, null, null, null, order, limit);

        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            throw new SQLException("No post found from database");
        } else {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                Long newId = cursor.getLong(0);
                int newSource = cursor.getInt(1);
                String newCategory = cursor.getString(2);
                String newTitle = cursor.getString(3);
                String newUrl = cursor.getString(4);
                String newAuthor = cursor.getString(5);
                long newDate = cursor.getLong(6);
                //date在数据库中的存储类型为integer（integer会根据数据的量级自动改变位数），date是长整型存储的所以要用getLong()，否则会丢失位数。
                String newMainBody = cursor.getString(7);

                post.setId(newId);
                post.setSource((byte) newSource);
                post.setCategory(newCategory);
                post.setTitle(newTitle);
                post.setUrl(newUrl);
                post.setAuthor(newAuthor);
                post.setDate(new Date(newDate));//new Date(newDate))是把整型的date数据转换成Date类型
                post.setMainBody(newMainBody);
                posts.add(new Post(post));
                //这里用了Post的拷贝构造方法进行深拷贝。
            }
        }
        return posts;
    }

    /**
     * 更新MainBody字段。
     *
     * @param thePost。 Post类型
     */
    public void updatePostInf(Post thePost) {
        if (thePost == null) {
            return;
        }
        ContentValues newPostValue = new ContentValues();
        Long rowIndex = thePost.getId();
        Cursor cursor = db
                .query(DATABASE_POST_TABLE, null, KEY_POST_ID + "=" + rowIndex, null, null, null,
                        null);
        cursor.moveToFirst();
        if (cursor.getString(7) != null) {
            if (!(cursor.getString(7).equals(thePost.getMainBody()))) {
                newPostValue.put(KEY_MAINBODY, thePost.getMainBody());
                db.update(DATABASE_POST_TABLE, newPostValue, KEY_POST_ID + "=" + rowIndex, null);
            }
        } else {
            newPostValue.put(KEY_MAINBODY, thePost.getMainBody());
            db.update(DATABASE_POST_TABLE, newPostValue, KEY_POST_ID + "=" + rowIndex, null);
        }
    }

}