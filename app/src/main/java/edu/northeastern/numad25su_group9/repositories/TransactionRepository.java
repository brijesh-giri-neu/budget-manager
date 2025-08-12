package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import edu.northeastern.numad25su_group9.models.Transaction;

/**
 * Repository for accessing transactions.
 * <p>
 * This class is responsible only for interacting with the database and
 * returning raw {@link Task} objects.
 * </p>
 */
public class TransactionRepository extends BaseRepository {

    /**
     * Constructs a TransactionRepository for a specific user.
     *
     * @param userId The fireBase UUID of the user.
     */
    public TransactionRepository(String userId) {
        super(FirebaseDatabase.getInstance().getReference("transactions").child(userId));
    }

    /**
     * Adds a new transaction to Firebase.
     *
     * @param txn           The {@link Transaction} object to be stored
     * @return {@link Task} representing the asynchronous write operation
     */
    public Task<Void> addTransaction(Transaction txn) {
        Map<String, Object> map = txn.toMap();
        map.put("createdAt", ServerValue.TIMESTAMP);
        map.put("updatedAt", ServerValue.TIMESTAMP);

        String transactionId = String.valueOf(txn.getTransactionDate());
        if (transactionId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Transaction date is null"));
            return tcs.getTask();
        }
        return set(transactionId, map);
    }

    /**
     * Updates an existing transaction in Firebase.
     *
     * @param txn           The updated {@link Transaction} object
     * @return {@link Task} representing the asynchronous update operation
     */
    public Task<Void> updateTransaction(Transaction txn) {
        Map<String, Object> map = txn.toMap();
        map.put("updatedAt", ServerValue.TIMESTAMP);

        String transactionId = String.valueOf(txn.getTransactionDate());
        if (transactionId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Transaction date is null"));
            return tcs.getTask();
        }

        return set(transactionId, map);
    }

    /**
     * Deletes a transaction from Firebase.
     *
     * @param transactionId The ID of the transaction to delete
     * @return {@link Task} representing the asynchronous delete operation
     */
    public Task<Void> deleteTransaction(String transactionId) {
        if (transactionId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Transaction Id is null"));
            return tcs.getTask();
        }
        return delete(transactionId);
    }

    /**
     * Retrieves a single transaction by its ID from Firebase.
     *
     * @param transactionId The ID of the transaction to retrieve
     * @return {@link Task} containing a {@link DataSnapshot} of the transaction
     */
    public Task<DataSnapshot> getTransactionById(String transactionId) {
        if (transactionId == null) {
            TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Transaction date is null"));
            return tcs.getTask();
        }
        return get(transactionId);
    }

    /**
     * Retrieves all transactions for the current user from Firebase.
     *
     * @return {@link Task} containing a {@link DataSnapshot} of all transactions
     */
    public Task<DataSnapshot> getAllTransactions() {
        return getAll();
    }

    public Task<DataSnapshot> getTransactionsByCategory(String categoryId) {
        return ref.orderByChild("categoryId")
                .equalTo(categoryId)
                .get();
    }

    public Task<DataSnapshot> getTransactionsByDateRange(long startMillis, long endMillis) {
        return ref.orderByChild("transactionDate")
                .startAt(startMillis)
                .endAt(endMillis)
                .get();
    }
}
