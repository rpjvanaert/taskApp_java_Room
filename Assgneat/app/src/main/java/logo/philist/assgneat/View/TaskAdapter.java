package logo.philist.assgneat.View;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import logo.philist.assgneat.Data.Task;
import logo.philist.assgneat.R;
import logo.philist.assgneat.View.Dialogs.DeleteTaskDialog;
import logo.philist.assgneat.View.Dialogs.DeleteTaskDialogShow;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> implements Observer<List<Task>> {

    private final DeleteTaskDialogShow deleteTaskDialogShow;
    private List<Task> tasks = new ArrayList<>();
    private final TaskEditor taskEditor;

    public TaskAdapter(TaskEditor callbackTask, DeleteTaskDialogShow deleteTaskDialogShow){
        this.taskEditor = callbackTask;
        this.deleteTaskDialogShow = deleteTaskDialogShow;
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task task = tasks.get(position);

        holder.textviewTaskName.setText(task.getName());
        holder.checkBoxDone.setOnCheckedChangeListener(null);
        holder.checkBoxDone.setChecked(task.isCheck());

        holder.checkBoxDone.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.i(TaskAdapter.class.getName(), holder.checkBoxDone.hashCode() + " button has been pressed to " + b);
            task.setCheck(b);
            taskEditor.updateCheck(task);
        });

        holder.fabCheck.setOnClickListener(view -> {
            task.setCheck(!task.isCheck());
            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
            holder.detailLayout.setVisibility(View.GONE);
            holder.buttonExpand.setBackgroundResource(R.drawable.ic_expand);
            taskEditor.updateCheck(task);
            notifyItemChanged(position);
        });

        holder.buttonExpand.setBackgroundResource(R.drawable.ic_expand);

        holder.textViewDescription.setText(task.getDescription());

        holder.fabDelete.setOnClickListener((view -> {
//            callbackTask.deleteTask(task);
//            DeleteTaskDialog dialog = new DeleteTaskDialog(taskEditor, task);
            this.deleteTaskDialogShow.delete(task);
        }));

        holder.fabEdit.setOnClickListener(view -> {
            taskEditor.editTask(task);
        });

        holder.buttonExpand.setOnClickListener(view -> {
            if (holder.detailLayout.getVisibility()==View.GONE){
                TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                holder.detailLayout.setVisibility(View.VISIBLE);
                holder.buttonExpand.setBackgroundResource(R.drawable.ic_collapse);
            } else {
                TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                holder.detailLayout.setVisibility(View.GONE);
                holder.buttonExpand.setBackgroundResource(R.drawable.ic_expand);
            }
        });

        holder.detailLayout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void logTasks(List<Task> tasks){
        Log.i(TaskAdapter.class.getName(), "Amount of tasks: " + tasks.size());
    }

    public Task getTaskAt(int position){
        return tasks.get(position);
    }

    @Override
    public void onChanged(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    class TaskHolder extends RecyclerView.ViewHolder {

        private MaterialTextView textviewTaskName;
        private MaterialCheckBox checkBoxDone;
        private ImageButton buttonExpand;

        private CardView cardView;
        private ConstraintLayout detailLayout;

        private MaterialTextView textViewDescription;
        private ExtendedFloatingActionButton fabCheck;
        private ExtendedFloatingActionButton fabEdit;
        private ExtendedFloatingActionButton fabDelete;

        public TaskHolder(@NonNull View itemView) {
            super(itemView);
            textviewTaskName = itemView.findViewById(R.id.textview_name);
            checkBoxDone = itemView.findViewById(R.id.radiobutton_check);
            buttonExpand = itemView.findViewById(R.id.button_expand);

            cardView = itemView.findViewById(R.id.cardview);
            detailLayout = itemView.findViewById(R.id.card_detail_layout);

            textViewDescription = itemView.findViewById(R.id.textview_description);
            fabCheck = itemView.findViewById(R.id.FAB_check);
            fabEdit = itemView.findViewById(R.id.FAB_edit);
            fabDelete = itemView.findViewById(R.id.FAB_delete);
        }
    }
}
