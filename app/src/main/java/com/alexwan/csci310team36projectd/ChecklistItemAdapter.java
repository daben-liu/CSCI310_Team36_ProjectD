package com.alexwan.csci310team36projectd;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alexwan.csci310team36projectd.data.model.ChecklistElement.ChecklistItem;
import java.util.List;

public class ChecklistItemAdapter extends RecyclerView.Adapter<ChecklistItemAdapter.ChecklistItemViewHolder> {

    private final List<ChecklistItem> items;

    public ChecklistItemAdapter(List<ChecklistItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ChecklistItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist_element, parent, false);
        return new ChecklistItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ChecklistItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;
        ImageButton deleteButton;

        ChecklistItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checklist_item_checkbox);
            editText = itemView.findViewById(R.id.checklist_item_text);
            deleteButton = itemView.findViewById(R.id.delete_checklist_item_button);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    items.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }

        void bind(final ChecklistItem item) {
            // Remove previous listeners to prevent conflicts
            editText.removeTextChangedListener((TextWatcher) editText.getTag());

            // Set data
            checkBox.setChecked(item.isChecked());
            editText.setText(item.getText());

            // Add new listeners
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> item.setChecked(isChecked));

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    item.setText(s.toString());
                }
            };
            editText.addTextChangedListener(textWatcher);
            editText.setTag(textWatcher); // Store the watcher to remove it later
        }
    }
}
