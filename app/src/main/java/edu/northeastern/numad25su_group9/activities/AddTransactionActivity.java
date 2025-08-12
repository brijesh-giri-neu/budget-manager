package edu.northeastern.numad25su_group9.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.databinding.ActivityAddTransactionBinding;
import edu.northeastern.numad25su_group9.models.Category;
import edu.northeastern.numad25su_group9.models.Transaction;
import edu.northeastern.numad25su_group9.services.CategoryService;
import edu.northeastern.numad25su_group9.services.LocationService;
import edu.northeastern.numad25su_group9.services.TransactionService;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AddTransactionActivity";

    // If editing existing transaction, pass transaction in intent
    public static final String TRANSACTION_EXTRA_KEY = "TRANSACTION";
    private static final String EDITING_KEY = "editing";
    private static final String SAVE_LOCATION_KEY = "saveLocation";

    private ActivityAddTransactionBinding binding;
    private Transaction transaction;
    private LocalDateTime selectedDateTime;
    private boolean editingExisting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            // If no saved information, get it from intent
            transaction = getIntent().getParcelableExtra(TRANSACTION_EXTRA_KEY);
            if (transaction != null && transaction.getTransactionId() != null && !transaction.getTransactionId().isBlank()) {
                editingExisting = true;
            }
        } else {
            // If existing information is saved, use it
            transaction = savedInstanceState.getParcelable(TRANSACTION_EXTRA_KEY);
            editingExisting = savedInstanceState.getBoolean(EDITING_KEY);
            binding.addTransactionLocation.setChecked(savedInstanceState.getBoolean(SAVE_LOCATION_KEY));
        }

        if (transaction == null) {
            transaction = new Transaction();
            autofillFromLocation();
        } else {
            binding.addTransactionAmount.setText(String.format(Locale.US, "$%.2f", transaction.getAmount()));
            binding.addTransactionDescription.setText(transaction.getDescription());
            binding.addTransactionIgnore.setChecked(transaction.isIgnore());
        }
        getCategories(transaction.getCategoryName());
        addCurrencyFormattingToAmount();
        setupDateView();

        if (editingExisting) {
            // If existing transaction, show delete button
            addDeleteButton();
            binding.addTransactionTitle.setText(R.string.edit_transaction);
            binding.addTransactionLocation.setVisibility(View.GONE);
        }

        binding.addTransactionCategory.setMinimumWidth(binding.addTransactionDescription.getWidth());

        binding.addTransactionSaveButton.setOnClickListener(v -> saveTransaction());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (transaction == null) {
            transaction = new Transaction();
            savedInputFieldsToTransaction();
        }
        outState.putParcelable(TRANSACTION_EXTRA_KEY, transaction);
        outState.putBoolean(EDITING_KEY, editingExisting);
        outState.putBoolean(SAVE_LOCATION_KEY, binding.addTransactionLocation.isChecked());

        super.onSaveInstanceState(outState);
    }

    private void savedInputFieldsToTransaction() {
        transaction.setAmount(Double.parseDouble(binding.addTransactionAmount.getText().toString().replaceAll("[$,]", "")));
        transaction.setDescription(binding.addTransactionDescription.getText().toString());
        transaction.setCategoryName(binding.addTransactionCategory.getSelectedItem().toString());
        transaction.setTransactionDateLocal(selectedDateTime);
        transaction.setIgnore(binding.addTransactionIgnore.isChecked());
    }

    private void autofillFromLocation() {
        LocationService.getInstance(this).getCurrentLocation(new LocationService.CurrentLocationCallback() {
            @Override
            public void onLocationAvailable(Location location) {
                transaction.setLatitude(location.getLatitude());
                transaction.setLongitude(location.getLongitude());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting current location", e);
                Toast.makeText(AddTransactionActivity.this, "Error getting current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCurrencyFormattingToAmount() {
        binding.addTransactionAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                binding.addTransactionAmount.removeTextChangedListener(this);

                String currentText = editable.toString();
                String cleanString = currentText.replaceAll("[$,.]", "");

                if (!cleanString.isEmpty()) {
                    double parsed = Double.parseDouble(cleanString);
                    String formatted = String.format(Locale.US, "$%.2f", parsed / 100);
                    binding.addTransactionAmount.setText(formatted);
                    binding.addTransactionAmount.setSelection(formatted.length());
                } else {
                    binding.addTransactionAmount.setText("$");
                    binding.addTransactionAmount.setSelection(1);
                }

                if (!currentText.startsWith("$")) {
                    binding.addTransactionAmount.setText("$" + currentText);
                    binding.addTransactionAmount.setSelection(binding.addTransactionAmount.getText().length());
                }

                binding.addTransactionAmount.addTextChangedListener(this);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });
    }

    private void setupDateView() {
        if (selectedDateTime == null) {
            if (transaction.getTransactionDate() == 0) {
                selectedDateTime = LocalDateTime.now();
            }
            else {
                selectedDateTime = transaction.getTransactionDateLocal();
            }
        }
        binding.addTransactionDate.setText(formatDate(selectedDateTime));


        binding.addTransactionDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDateTime = selectedDateTime.withYear(year);
                selectedDateTime = selectedDateTime.withMonth(month + 1); // Month is 0-indexed in DatePickerDialog
                selectedDateTime = selectedDateTime.withDayOfMonth(dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                    selectedDateTime = selectedDateTime.withHour(hourOfDay);
                    selectedDateTime = selectedDateTime.withMinute(minute);
                    binding.addTransactionDate.setText(formatDate(selectedDateTime));
                }, selectedDateTime.getHour(), selectedDateTime.getMinute(), false);

                timePickerDialog.show();

            }, selectedDateTime.getYear(), selectedDateTime.getMonthValue() - 1, selectedDateTime.getDayOfMonth());
            datePickerDialog.show();
        });
    }

    @NonNull
    private static String formatDate(LocalDateTime date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.SHORT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT);
        return String.format(Locale.US, "%s %s", date.format(dateFormatter), date.format(timeFormatter));
    }

    private void saveTransaction() {
        if (transaction == null) {
            Toast.makeText(this, "No information to save", Toast.LENGTH_SHORT).show();
            return;
        }

        savedInputFieldsToTransaction();

        TransactionService transactionService = new TransactionService();

        // New transaction
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isBlank()) {
            Log.d(TAG, "Saving new transaction");
            if (!binding.addTransactionLocation.isChecked()) {
                transaction.setLatitude(null);
                transaction.setLongitude(null);
            }
            transactionService.addTransaction(transaction, new TransactionService.OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddTransactionActivity.this, "New Transaction saved!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error saving new transaction", e);
                    Toast.makeText(AddTransactionActivity.this, "Error saving new transaction", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Log.d(TAG, "Updating existing transaction");
            transactionService.updateTransaction(transaction, new TransactionService.OperationCallback() {

                @Override
                public void onSuccess() {
                    Toast.makeText(AddTransactionActivity.this, "Transaction updated!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error updating transaction", e);
                    Toast.makeText(AddTransactionActivity.this, "Error updating transaction", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    private void getCategories(@Nullable String savedCategory) {
        new CategoryService().getAllCategories(new CategoryService.CategoriesCallback() {

            @Override
            public void onSuccess(List<Category> categoryList) {
                if (binding == null) {
                    return;
                }
                List<String> categoryNameList = categoryList.stream().map(Category::getName).collect(Collectors.toList());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTransactionActivity.this, android.R.layout.simple_spinner_item, categoryNameList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.addTransactionCategory.setAdapter(adapter);

                if (savedCategory != null && !savedCategory.isBlank()) {
                    for (int i = 0; i < categoryNameList.size(); i++) {
                        if (categoryNameList.get(i).equals(savedCategory)) {
                            binding.addTransactionCategory.setSelection(i);
                            break;
                        }
                    }
                } else {
                    if (transaction != null && transaction.getCategoryName() != null) {
                        for (int i = 0; i < categoryNameList.size(); i++) {
                            if (categoryNameList.get(i).equals(transaction.getCategoryName())) {
                                binding.addTransactionCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting categories", e);
                Toast.makeText(AddTransactionActivity.this, "Error getting categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDeleteButton() {
        binding.addTransactionDeleteButton.setVisibility(View.VISIBLE);
        binding.addTransactionDeleteButton.setOnClickListener((view -> {
            new TransactionService().deleteTransaction(transaction.getTransactionId(), new TransactionService.OperationCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Transaction deleted");
                    Toast.makeText(AddTransactionActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error deleting transaction", e);
                    Toast.makeText(AddTransactionActivity.this, "Error deleting transaction", Toast.LENGTH_SHORT).show();
                }
            });

        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}