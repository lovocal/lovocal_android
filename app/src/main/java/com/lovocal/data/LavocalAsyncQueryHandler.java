

package com.lovocal.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.lovocal.LavocalApplication;
import com.lovocal.data.DBInterface.AsyncDbQueryCallback;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Class to encapsulate asynchronous database queries. This class should not be
 * used directly. Instead, send in your queries through the
 * <code>DBUtils.async()</code> methods.
 * 
 * @author Anshul Kamboj
 */
@SuppressLint("UseSparseArrays")
class LavocalAsyncQueryHandler {

    /**
     * The type of DB Query
     */
    private enum Type {
        INSERT,
        UPDATE,
        DELETE,
        QUERY
    }

    /**
     * Map of tokens to async tasks
     */
    private final Queue<QueryTask> mTaskQueue;

    LavocalAsyncQueryHandler() {

        /*
         * Don't worry about sparse array optimization. Real world tests have
         * shown that the performance benefits are seen only if the data size is
         * > 10k or so, in which case we should be worrying about our
         * implementation, rather than performance
         */

        //http://www.javacodegeeks.com/2012/07/android-performance-tweaking-parsearray.html
        mTaskQueue = new LinkedList<QueryTask>();
    }

    /**
     * Method for inserting rows into the database
     *
     * @param taskId Unique id for this operation
     * @param tag An object to tag this task for cancellation
     * @param cookie Any extra object to be passed into the query to be returned
     *            when the query completes. Can be <code>null</code>
     * @param table The table to insert into
     * @param nullColumnHack column names are known and an empty row can't be
     *            inserted. If not set to null, the nullColumnHack parameter
     *            provides the name of nullable column name to explicitly insert
     *            a NULL into in the case where your values is empty.
     * @param values The fields to insert
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startInsert(final int taskId, final Object tag, final Object cookie,
                    final String table, final String nullColumnHack,
                    final ContentValues values, final boolean autoNotify,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.INSERT, taskId, tag, cookie, callback);
        task.mTableName = table;
        task.mNullColumnHack = nullColumnHack;
        task.mValues = values;
        task.mAutoNotify = autoNotify;
        mTaskQueue.add(task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);
    }

    /**
     * Delete rows from the database
     *
     * @param taskId A unique id for this operation
     * @param tag An object to tag this request for cancellation
     * @param cookie Any extra object to be passed into the query to be returned
     *            when the query completes. Can be <code>null</code>
     * @param table The table to delete from
     * @param selection The WHERE clause
     * @param selectionArgs Arguments for the where clause
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startDelete(final int taskId, final Object tag, final Object cookie,
                    final String table, final String selection,
                    final String[] selectionArgs, final boolean autoNotify,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.DELETE, taskId, tag, cookie, callback);
        task.mTableName = table;
        task.mSelection = selection;
        task.mSelectionArgs = selectionArgs;
        task.mAutoNotify = autoNotify;
        mTaskQueue.add(task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);

    }

    /**
     * Updates the table with the given data
     *
     * @param taskId A unique id for this operation
     * @param tag A tag to cancel this request
     * @param cookie Any extra object to be passed into the query to be returned
     *            when the query completes. Can be <code>null</code>
     * @param table The table to update
     * @param values The fields to update
     * @param selection The WHERE clause
     * @param selectionArgs Arguments for the where clause
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startUpdate(final int taskId, final Object tag, final Object cookie,
                    final String table, final ContentValues values,
                    final String selection, final String[] selectionArgs,
                    final boolean autoNotify,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.UPDATE, taskId, tag, cookie, callback);
        task.mTableName = table;
        task.mValues = values;
        task.mSelection = selection;
        task.mSelectionArgs = selectionArgs;
        task.mAutoNotify = autoNotify;
        mTaskQueue.add(task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);

    }

    /**
     * Query the given table, returning a Cursor over the result set.
     *
     * @param taskId A unique id for this query
     * @param tag A tag for task cancellation
     * @param cookie Any extra object to be passed into the query to be returned
     *            when the query completes. Can be <code>null</code>
     * @param distinct <code>true</code> if dataset should be unique
     * @param table The table to query
     * @param columns The columns to fetch
     * @param selection The selection string, formatted as a WHERE clause
     * @param selectionArgs The arguments for the selection parameter
     * @param groupBy GROUP BY clause
     * @param having HAVING clause
     * @param orderBy ORDER BY clause
     * @param limit LIMIT clause
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startQuery(final int taskId, final Object tag, final Object cookie,
                    final boolean distinct, final String table,
                    final String[] columns, final String selection,
                    final String[] selectionArgs, final String groupBy,
                    final String having, final String orderBy,
                    final String limit, final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.QUERY, taskId, tag, cookie, callback);
        task.mDistinct = distinct;
        task.mTableName = table;
        task.mColumns = columns;
        task.mSelection = selection;
        task.mSelectionArgs = selectionArgs;
        task.mGroupBy = groupBy;
        task.mHaving = having;
        task.mOrderBy = orderBy;
        task.mLimit = limit;

        mTaskQueue.add(task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);

    }

    /**
     * Cancels any pending async operations
     *
     * @param token The token to cancel
     */
    void cancel(final Object tag) {

        for (final Iterator<QueryTask> it = mTaskQueue.iterator(); it.hasNext();) {
            final QueryTask task = it.next();
            if (task.mTag.equals(tag)) {
                task.mCancelled = true;
                task.mCallback = null;
                it.remove();
            }
        }
    }

    /**
     * Class that holds the result of the DB Operation
     */
    private static final class QueryResult {

        /**
         * The task that triggered this result
         */
        private final QueryTask mTask;

        /**
         * Inserted row id in case of an insert operation
         */
        private long            mInsertRowId;

        /**
         * Update count in case of an update operation
         */
        private int             mUpdateCount;

        /**
         * Delete count in case of a delete operation
         */
        private int             mDeleteCount;

        /**
         * Cursor in case of a query operation
         */
        private Cursor mCursor;

        /**
         * Construct a query result
         *
         * @param task The {@link QueryTask} that triggered this result
         */
        private QueryResult(final QueryTask task) {
            mTask = task;
        }
    }

    /**
     * @author Vinay S Shenoy Clas representing a query task
     */
    private static final class QueryTask {

        /**
         * Type of the operation, whether it is INSERT, UPDATE, DELETE or QUERY
         */
        private final Type           mType;

        /**
         * The tag to use for cancellation
         */
        private final Object mTag;

        /**
         * Any extra cookie sent along with the query
         */
        private final Object mCookie;

        /**
         * An identifier for this task
         */
        private final int            mTaskId;

        /**
         * Callback for the result of the operation
         */
        private AsyncDbQueryCallback mCallback;

        private String mTableName;
        private boolean              mDistinct;
        private String[]             mColumns;
        private String mSelection;
        private String[]             mSelectionArgs;
        private String mGroupBy;
        private String mOrderBy;
        private String mHaving;
        private String mLimit;
        private String mNullColumnHack;
        private ContentValues mValues;
        private boolean              mAutoNotify;

        private boolean              mCancelled;

        /**
         * Construct a Query Task
         *
         * @param type The {@link Type} of task
         * @param taskId An identifier for this task
         * @param tag An object to tag this request for cancellation
         * @param cookie Any extra object to be passed into the query to be
         *            returned when the query completes. Can be
         *            <code>null</code>
         * @param callback The Callback for when the db query completes
         */
        private QueryTask(final Type type, final int taskId, final Object tag, final Object cookie, final AsyncDbQueryCallback callback) {
            mType = type;
            mTaskId = taskId;
            mCookie = cookie;
            mCallback = callback;
            mCancelled = false;
            mTag = tag;
        }

    }

    /**
     * Custom AsyncTask to do the background work for database operations
     * 
     * @author Vinay S Shenoy
     */
    private class QueryAsyncTask extends
            AsyncTask<QueryTask, Void, QueryResult> {

        @Override
        protected QueryResult doInBackground(final QueryTask... params) {

            final QueryTask task = params[0];
            final QueryResult result = new QueryResult(task);

            if (task.mCancelled) {
                return result;
            }

            switch (task.mType) {
                case INSERT: {
                    result.mInsertRowId = LavocalSQLiteOpenHelper
                                    .getInstance(LavocalApplication.getStaticContext())
                                    .insert(task.mTableName, task.mNullColumnHack, task.mValues, task.mAutoNotify);
                    break;
                }

                case DELETE: {
                    result.mDeleteCount = LavocalSQLiteOpenHelper
                                    .getInstance(LavocalApplication.getStaticContext())
                                    .delete(task.mTableName, task.mSelection, task.mSelectionArgs, task.mAutoNotify);
                    break;
                }

                case UPDATE: {
                    result.mUpdateCount = LavocalSQLiteOpenHelper
                                    .getInstance(LavocalApplication.getStaticContext())
                                    .update(task.mTableName, task.mValues, task.mSelection, task.mSelectionArgs, task.mAutoNotify);
                    break;

                }

                case QUERY: {
                    result.mCursor = LavocalSQLiteOpenHelper
                                    .getInstance(LavocalApplication.getStaticContext())
                                    .query(task.mDistinct, task.mTableName, task.mColumns, task.mSelection, task.mSelectionArgs, task.mGroupBy, task.mHaving, task.mOrderBy, task.mLimit);
                    break;
                }
            }

            return result;

        }

        @Override
        protected void onPostExecute(final QueryResult result) {

            final QueryTask task = result.mTask;
            if (!task.mCancelled) {
                switch (task.mType) {

                    case INSERT: {
                        task.mCallback.onInsertComplete(task.mTaskId, task.mCookie, result.mInsertRowId);
                        break;
                    }

                    case DELETE: {
                        task.mCallback.onDeleteComplete(task.mTaskId, task.mCookie, result.mDeleteCount);
                        break;
                    }

                    case UPDATE: {
                        task.mCallback.onUpdateComplete(task.mTaskId, task.mCookie, result.mUpdateCount);
                        break;
                    }

                    case QUERY: {
                        task.mCallback.onQueryComplete(task.mTaskId, task.mCookie, result.mCursor);
                        break;
                    }
                }
            }
            mTaskQueue.remove(task);

        }
    }

}
