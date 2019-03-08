/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.recyclerviewAdapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;

public class FingerprintCapturedAdapter extends RecyclerView.Adapter<FingerprintCapturedAdapter.FingerprintCapturedHolder> {

    private final List<String> mFingerprintCapturedList = new ArrayList<>();
    private final Set<String> mFingerprintCapturedSet = new HashSet<>();
    private final FingerItemRemoveClickedListener mFingerItemRemoveClickedListener;

    public FingerprintCapturedAdapter(FingerItemRemoveClickedListener fingerItemRemoveClickedListener){
        mFingerItemRemoveClickedListener = fingerItemRemoveClickedListener;
    }

    @NonNull
    @Override
    public FingerprintCapturedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.captured_list_item,parent,false);
        return new FingerprintCapturedHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FingerprintCapturedHolder holder, int position) {
        holder.bindData(mFingerprintCapturedList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFingerprintCapturedList.size();
    }

    public void  addFingerprintPosition(String position)
    {
        mFingerprintCapturedSet.add(position);
        mFingerprintCapturedList.clear();
        mFingerprintCapturedList.addAll(mFingerprintCapturedSet);
        notifyDataSetChanged();

    }

    public void removeFingerprintPosition(String fingerPosition){
        mFingerprintCapturedList.remove(fingerPosition);
        mFingerprintCapturedSet.remove(fingerPosition);
        notifyDataSetChanged();
    }

    public void clear() {
        mFingerprintCapturedSet.clear();
        mFingerprintCapturedList.clear();
        notifyDataSetChanged();
    }

    class FingerprintCapturedHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.itemTxt)
        TextView fingerprintPosition;

        @BindView(R.id.removeItemBtn)
        Button removeItemBtn;
        FingerprintCapturedHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        void bindData(String position)
        {
            removeItemBtn.setOnClickListener(this);
            fingerprintPosition.setText(position);
        }


        @Override
        public void onClick(View v) {
            int itemSelected = getAdapterPosition();
            mFingerItemRemoveClickedListener.removeFingerItem(mFingerprintCapturedList.get(itemSelected));
        }
    }


    public interface FingerItemRemoveClickedListener {
        void removeFingerItem(String fingerToRemove);
    }
}
