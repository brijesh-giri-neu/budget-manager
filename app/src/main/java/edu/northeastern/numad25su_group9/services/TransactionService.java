package edu.northeastern.numad25su_group9.services;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad25su_group9.models.Transaction;
import edu.northeastern.numad25su_group9.repositories.TransactionRepository;

/**
 * Service layer for interacting with transactions.
 */
public class TransactionService {
    public static final String TRANSACTION_SERVICE = "TransactionService";
    private final TransactionRepository repository;
    private final SpendingService spendService;

    /**
     * Constructs a TransactionService using the current authenticated user.
     */
    public TransactionService() {
        String userId = new AuthService().getCurrentUserId();
        this.repository = new TransactionRepository(userId);
        this.spendService = new SpendingService();
    }

    // Callbacks
    /**
     * Callback for operations returning a list of transactions.
     */
    public interface TransactionsCallback {
        /**
         * Called when the operation completes successfully.
         *
         * @param transactions The resulting list of {@link Transaction} objects
         */
        void onSuccess(List<Transaction> transactions);

        /**
         * Called when the operation fails.
         *
         * @param e Exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * Callback for operations returning a single transaction.
     */
    public interface TransactionCallback {
        /**
         * Called when the operation completes successfully.
         *
         * @param transaction The resulting {@link Transaction} object
         */
        void onSuccess(Transaction transaction);

        /**
         * Called when the operation fails.
         *
         * @param e Exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * Callback for operations that only signal success or failure without a return value.
     */
    public interface OperationCallback {
        /**
         * Called when the operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param e Exception describing the failure
         */
        void onError(Exception e);
    }

    // Methods

    /**
     * Adds a new transaction to Firebase.
     * Uses transactionDate to uniquely identify transactions for a user.
     * NOTE: Uses transactionDate as a transactionId.
     *
     * @param transaction   The {@link Transaction} object to store
     * @param callback      Callback to report success or error
     */
    public void addTransaction(Transaction transaction, OperationCallback callback) {
        Log.d(TRANSACTION_SERVICE, "addTransaction: adding transaction: " + transaction.toString() + " to Firebase");
        repository.addTransaction(transaction)
                .addOnSuccessListener(unused -> {
                    if (transaction.isIgnore()) {
                        callback.onSuccess();
                        return;
                    }

                    spendService.incrementSpend(transaction, +transaction.getAmount(),
                            new SpendingService.OperationCallback() {
                                @Override public void onSuccess() { callback.onSuccess(); }
                                @Override public void onError(Exception e) { callback.onError(e); }
                            });
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Updates an existing transaction in Firebase.
     * NOTE: Need the tranasctionId field populated for the update to work.
     *
     * @param updated   The updated {@link Transaction} object
     * @param callback      Callback to report success or error
     */
    public void updateTransaction(Transaction updated, OperationCallback callback) {
        Log.d(TRANSACTION_SERVICE, "updateTransaction: updating transaction: " + updated.toString() + " in Firebase");
        String transactionId = updated.getTransactionId();

        // Do a read first to get a copy of the Transaction object before update.
        repository.getTransactionById(transactionId)
                .addOnSuccessListener(oldSnap -> {
                    if (!oldSnap.exists()) {
                      callback.onError(new Exception("The transaction to be updated does not exist."));
                      return;
                    }

                    Transaction oldTxn = oldSnap.getValue(Transaction.class);

                    // Push the required update.
                    repository.updateTransaction(updated)
                            .addOnSuccessListener(u -> {
                                // If no previous txn, just apply new delta if needed
                                if (oldTxn == null) {
                                    if (updated.isIgnore()) { callback.onSuccess(); return; }
                                    spendService.incrementSpend(updated, +updated.getAmount(),
                                            new SpendingService.OperationCallback() {
                                                @Override public void onSuccess() { callback.onSuccess(); }
                                                @Override public void onError(Exception e) { callback.onError(e); }
                                            });
                                    return;
                                }

                                // Chain: reverse old (if not ignored) + apply new (if not ignored)
                                if (!oldTxn.isIgnore()) {
                                    spendService.incrementSpend(oldTxn, -oldTxn.getAmount(),
                                            new SpendingService.OperationCallback() {
                                                @Override public void onSuccess() {
                                                    if (updated.isIgnore()) { callback.onSuccess(); return; }
                                                    spendService.incrementSpend(updated, +updated.getAmount(),
                                                            new SpendingService.OperationCallback() {
                                                                @Override public void onSuccess() { callback.onSuccess(); }
                                                                @Override public void onError(Exception e) { callback.onError(e); }
                                                            });
                                                }
                                                @Override public void onError(Exception e) { callback.onError(e); }
                                            });
                                } else {
                                    // Old was ignored; only add new if needed
                                    if (updated.isIgnore()) { callback.onSuccess(); return; }
                                    spendService.incrementSpend(updated, +updated.getAmount(),
                                            new SpendingService.OperationCallback() {
                                                @Override public void onSuccess() { callback.onSuccess(); }
                                                @Override public void onError(Exception e) { callback.onError(e); }
                                            });
                                }
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Deletes a transaction from Firebase.
     *
     * @param transactionId The ID of the transaction to delete
     * @param callback      Callback to report success or error
     */
    public void deleteTransaction(String transactionId, OperationCallback callback) {
        // Do a read first to get a copy of the Transaction object before update.
        repository.getTransactionById(transactionId)
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        callback.onError(new Exception("The transaction to be deleted does not exist."));
                        return;
                    }
                    Transaction existing = snap.getValue(Transaction.class);

                    // Push the required update.
                    repository.deleteTransaction(transactionId)
                            .addOnSuccessListener(u -> {
                                if (existing == null || existing.isIgnore()) {
                                    callback.onSuccess();
                                    return;
                                }
                                spendService.incrementSpend(existing, -existing.getAmount(),
                                        new SpendingService.OperationCallback() {
                                            @Override public void onSuccess() { callback.onSuccess(); }
                                            @Override public void onError(Exception e) { callback.onError(e); }
                                        });
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param transactionId The ID of the transaction to retrieve
     * @param callback      Callback returning the {@link Transaction} or an error
     */
    public void getTransactionById(String transactionId, TransactionCallback callback) {
        repository.getTransactionById(transactionId)
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError(new Exception("Transaction not found"));
                        return;
                    }
                    Transaction txn = snapshot.getValue(Transaction.class);

                    if (txn != null) {
                        txn.setTransactionId(snapshot.getKey());
                        callback.onSuccess(txn);
                    } else {
                        callback.onError(new Exception("Transaction data invalid"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Retrieves all transactions for the current user.
     *
     * @param callback Callback returning the list of {@link Transaction} objects or an error
     */
    public void getAllTransactions(TransactionsCallback callback) {
        repository.getAllTransactions()
                .addOnSuccessListener(snapshot -> {
                    // Return default value
                    if (!snapshot.exists()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Transaction> list = new ArrayList<>();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Transaction txn = snap.getValue(Transaction.class);
                        if (txn != null) {
                            txn.setTransactionId(snap.getKey());
                            list.add(txn);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Retrieves all transactions that occurred in a specific month and year in UTC time.
     *
     * @param year     The year to filter by (e.g., 2025)
     * @param month    The month to filter by (1-12)
     * @param callback Callback returning the filtered transactions or an error
     */
    public void getTransactionsByMonth(int year, int month, TransactionsCallback callback) {
        // First day of the month at 00:00 UTC
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        // Last nanosecond of the month
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        long startMillis = start.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        long endMillis = end.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

        repository.getTransactionsByDateRange(startMillis, endMillis)
                .addOnSuccessListener(snapshot -> {
                    // Return default value
                    if (!snapshot.exists()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Transaction> list = new ArrayList<>();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Transaction txn = snap.getValue(Transaction.class);
                        if (txn != null) {
                            txn.setTransactionId(snap.getKey());
                            list.add(txn);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Retrieves all transactions that belong to the specified category.
     *
     * @param categoryId The category ID to filter by
     * @param callback   Callback returning the filtered transactions or an error
     */
    public void getTransactionsByCategory(String categoryId, TransactionsCallback callback) {
        repository.getTransactionsByCategory(categoryId)
                .addOnSuccessListener(snapshot -> {
                    // Return default value
                    if (!snapshot.exists()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Transaction> list = new ArrayList<>();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Transaction txn = snap.getValue(Transaction.class);
                        if (txn != null) {
                            txn.setTransactionId(snap.getKey());
                            list.add(txn);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }
}
