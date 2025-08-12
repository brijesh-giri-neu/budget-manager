package edu.northeastern.numad25su_group9.fragments.budget;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAOptionsConstructor;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AAOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.northeastern.numad25su_group9.databinding.FragmentBudgetBinding;
import edu.northeastern.numad25su_group9.models.Budget;
import edu.northeastern.numad25su_group9.models.MonthlySpendingSummary;
import edu.northeastern.numad25su_group9.services.BudgetService;
import edu.northeastern.numad25su_group9.services.SpendingService;

public class BudgetFragment extends Fragment {
    private static final String TAG = "BudgetFragment";

    private FragmentBudgetBinding binding;

    public static BudgetFragment newInstance() {
        return new BudgetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBudgetBinding.inflate(inflater, container, false);

        checkItemVisibiltiy();

        setupBudgetButtonListener();
        getBudgetForText();
        drawYearChart(); // initial

        return binding.getRoot();
    }

    private void checkItemVisibiltiy() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.budgetChart.setVisibility(View.GONE);
        } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.budgetChart.setVisibility(View.VISIBLE);
        }
    }

    private void setupBudgetButtonListener() {
        binding.budgetSaveButton.setOnClickListener(view -> {
            try {

                // Remove $ and commas before parsing
                String budgetString = binding.budgetEditText.getText().toString().replaceAll("[$,]", "");
                double budget = Double.parseDouble(budgetString);

                if (budgetString.isEmpty() || budget < 0) {
                    getBudgetForText();
                    Toast.makeText(getContext(), "Invalid budget", Toast.LENGTH_SHORT).show();
                    return;
                }

                BudgetService budgetService = new BudgetService();
                Budget newBudget = new Budget.Builder()
                        .setAmount(budget)
                        .setMonthDateLocal(LocalDateTime.now())
                        .build();
                budgetService.addBudget(newBudget, new BudgetService.OperationCallback() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully added budget");
                        if (binding != null && isAdded()) {
                            getBudgetForText();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error adding budget", e);
                    }

                });

            } catch (NumberFormatException | NullPointerException e) {
                Log.e(TAG, "Error parsing budget", e);
                getBudgetForText();
                Toast.makeText(getContext(), "Invalid budget", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBudgetForText() {
        BudgetService budgetService = new BudgetService();
        budgetService.getLatestBudget(new BudgetService.BudgetCallback() {
            @Override
            public void onSuccess(Budget b) {
                Log.d(TAG, String.format("Successfully got latest budget: %s", b.getAmount()));
                if (binding != null && isAdded()) {
                    binding.budgetEditText.setText(String.format(Locale.US, "$%.0f", b.getAmount()));
                    binding.budgetSaveButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting latest budget", e);
                if (binding != null && isAdded()) {
                    Toast.makeText(getContext(), "Error getting budget", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.budgetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this use case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed for this use case
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Show the save button when text changes
                binding.budgetSaveButton.setVisibility(View.VISIBLE);

                binding.budgetEditText.removeTextChangedListener(this);

                String currentText = s.toString();
                String cleanString = currentText.replaceAll("[$,.]", "");

                if (!cleanString.isEmpty()) {
                    double parsed = Double.parseDouble(cleanString);
                    String formatted = String.format(Locale.US, "$%.0f", parsed);
                    binding.budgetEditText.setText(formatted);
                    binding.budgetEditText.setSelection(formatted.length());
                } else {
                    binding.budgetEditText.setText("$");
                    binding.budgetEditText.setSelection(1);
                }

                if (!currentText.startsWith("$")) {
                    binding.budgetEditText.setText("$" + currentText);
                    binding.budgetEditText.setSelection(binding.budgetEditText.getText().length());
                }

                binding.budgetEditText.addTextChangedListener(this);
            }
        });

    }

    /**
     * Build Remaining vs Total chart for the selectedYear.
     */
    private void drawYearChart() {
        if (binding.budgetChart.getVisibility() != View.VISIBLE) return;

        LocalDate date = LocalDate.now();

        BudgetService budgetService = new BudgetService();
        budgetService.getAllBudgetsForYear(date.getYear(), new BudgetService.BudgetsCallback() {
            @Override
            public void onSuccess(List<Budget> list) {
                if (binding == null) {
                    return;
                }

                Double[] budgets = new Double[12];
                Arrays.fill(budgets, 0.0);
                for (Budget b : list) {
                    budgets[b.getMonthDateLocal().getMonthValue() - 1] = b.getAmount();
                }

                new SpendingService().getSpendingByMonth(date.getYear(), new SpendingService.MonthlySpendingsCallback() {

                    @Override
                    public void onSuccess(List<MonthlySpendingSummary> list) {
                        Log.d(TAG, String.format("Successfully got %d spendings", list.size()));
                        if (binding == null) {
                            return;
                        }

//                        if (list.isEmpty()) {
//                            binding.budgetChartCard.setVisibility(View.GONE);
//                            return;
//                        }

                        Double[] spending = new Double[12];
                        Arrays.fill(spending, 0.0);
                        for (MonthlySpendingSummary s : list) {
                            spending[s.getMonth() - 1] = s.getAmount();
                        }

                        AAChartModel yearlyBudgetChartModel = new AAChartModel()
                                .chartType(AAChartType.Column) // Bar chart style
                                .title("Yearly Budget")
                                .categories(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"})
                                .dataLabelsEnabled(false)
                                .yAxisGridLineWidth(0f)
                                .series(new AASeriesElement[]{
                                        new AASeriesElement()
                                                .name("Budget")
                                                .data(budgets),
                                        new AASeriesElement()
                                                .name("Spending")
                                                .data(spending)
                                });

                        AAOptions options = AAOptionsConstructor.configureChartOptions(yearlyBudgetChartModel);
                        binding.budgetChart.aa_drawChartWithChartOptions(options);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error getting spending", e);
                        if (binding != null && isAdded()) {
                            binding.budgetChartCard.setVisibility(View.GONE);
                        }
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting budgets", e);
                if (binding != null && isAdded()) {
                    binding.budgetChartCard.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
