package com.begers.smartnote;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.begers.smartnote.databinding.RecyclerRowBinding;

import java.util.List;

public class AddAdapter extends RecyclerView.Adapter<AddAdapter.AddHolder> {

    List<Note> notes;

    public  AddAdapter(List<Note> notes){
        this.notes = notes;
    }
    @NonNull
    @Override
    public AddHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AddHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(notes.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),AddActivity.class);
                intent.putExtra("info", "old");
                intent.putExtra("noteId", notes.get(position).getId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class AddHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public AddHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
