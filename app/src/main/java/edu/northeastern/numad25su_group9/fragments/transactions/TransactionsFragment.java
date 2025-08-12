package edu.northeastern.numad25su_group9.fragments.transactions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad25su_group9.activities.AddTransactionActivity;
import edu.northeastern.numad25su_group9.databinding.FragmentTransactionsBinding;
import edu.northeastern.numad25su_group9.models.Transaction;
import edu.northeastern.numad25su_group9.services.TransactionService;

public class TransactionsFragment extends Fragment {

    // For logging
    private static final String TAG = "TransactionsFragment";
    private FragmentTransactionsBinding binding;
    private TransactionService transactionService;
    private TransactionAdapter transactionAdapter;

    public TransactionsFragment() {}

    public static TransactionsFragment newInstance() {
        return new TransactionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionService = new TransactionService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.transactionsProgressBar.setVisibility(View.VISIBLE);
        buildRecyclerView();
        binding.transactionsAddButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTransactions();
    }

    private void buildRecyclerView() {
        RecyclerView recyclerView = binding.transactionsRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(transactionAdapter);
    }

    private void updateTransactions() {
        binding.transactionsProgressBar.setVisibility(View.VISIBLE);

        // Fetch transactions from the service
        transactionService.getAllTransactions(new TransactionService.TransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> fetchedTransactions) {
                if (binding == null) {
                    return;
                }
                binding.transactionsProgressBar.setVisibility(View.INVISIBLE);

                if (fetchedTransactions.isEmpty()) {
                    Log.d(TAG, "No transactions found");
                }

                if (isAdded()) {
                    transactionAdapter.updateTransactions(fetchedTransactions);

//                    // Show toast on screen if empty
//                    if (fetchedTransactions.isEmpty()) {
//                        // Ensure the fragment is still attached to its activity before updating the UI.
//                        // Callbacks may be triggered after the user navigates away from this screen.
//                        requireActivity().runOnUiThread(() ->
//                            Toast.makeText(requireContext(), "No transactions found", Toast.LENGTH_SHORT).show()
//                        );
//                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching transactions", e);
                if (binding != null) {
                    binding.transactionsProgressBar.setVisibility(View.INVISIBLE);
                }
                // Ensure the fragment is still attached to its activity before updating the UI.
                // Callbacks may be triggered after the user navigates away from this screen.
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.transactionsRecyclerView.setAdapter(null);
        binding = null;
    }
}