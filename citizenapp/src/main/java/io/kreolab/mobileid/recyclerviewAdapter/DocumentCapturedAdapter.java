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

public class DocumentCapturedAdapter extends RecyclerView.Adapter<DocumentCapturedAdapter.DocumentCapturedHolder> {

    private final List<String> mDocumentCapturedList = new ArrayList<>();
    private final Set<String> mDocumentCapturedSet = new HashSet<>();

    private final DocumentItemRemoveClickedListener mDocumentItemRemoveClickedListener;

    public DocumentCapturedAdapter(DocumentItemRemoveClickedListener documentItemRemoveClickedListener){
        mDocumentItemRemoveClickedListener = documentItemRemoveClickedListener;
    }

    @NonNull
    @Override
    public DocumentCapturedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.captured_list_item,parent,false);
        return new DocumentCapturedHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DocumentCapturedHolder holder, int position) {
        holder.bindData(mDocumentCapturedList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDocumentCapturedList.size();
    }

    public void  addDocument(String position)
    {
        mDocumentCapturedSet.add(position);
        mDocumentCapturedList.clear();
        mDocumentCapturedList.addAll(mDocumentCapturedSet);
        notifyDataSetChanged();

    }

    public  void removeDocument(String documentItem){
        mDocumentCapturedList.remove(documentItem);
        mDocumentCapturedSet.remove(documentItem);
        notifyDataSetChanged();
    }

    public void clear() {
        mDocumentCapturedSet.clear();
        mDocumentCapturedList.clear();
        notifyDataSetChanged();
    }

    class DocumentCapturedHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.itemTxt)
        TextView fingerprintPosition;

        @BindView(R.id.removeItemBtn)
        Button mRemoveItemBtn;
        DocumentCapturedHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        void bindData(String position){
            fingerprintPosition.setText(position);
            mRemoveItemBtn.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int itemSelected = getAdapterPosition();
            mDocumentItemRemoveClickedListener.removeDocumentItem(mDocumentCapturedList.get(itemSelected));
        }
    }


    public interface DocumentItemRemoveClickedListener {
        void removeDocumentItem(String documentToRemove);
    }
}
