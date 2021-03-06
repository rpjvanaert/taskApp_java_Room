package logo.philist.assgneat.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import logo.philist.assgneat.Data.Task;
import logo.philist.assgneat.R;
import logo.philist.assgneat.View.Dialogs.DeleteAllTasksDialog;
import logo.philist.assgneat.View.Dialogs.DeleteTaskDialog;
import logo.philist.assgneat.View.Dialogs.DeleteTaskDialogShow;
import logo.philist.assgneat.ViewModel.TaskViewModel;

public class MainActivity extends AppCompatActivity implements TaskEditor, DeleteTaskDialogShow {

    public static final int ADD_TASK_REQUEST = 1;
    public static final int EDIT_TASK_REQUEST = 2;

    private TaskViewModel taskViewModel;

    private ExtendedFloatingActionButton fabAddTask;
    private ExtendedFloatingActionButton fabDeleteAllTasks;
    private ExtendedFloatingActionButton fabMenu;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init(){
        Toolbar toolbar = findViewById(R.id.toolbar);

        fabAddTask = findViewById(R.id.FAB_add);
        fabAddTask.shrink();
        fabDeleteAllTasks = findViewById(R.id.FAB_delete_all);
        fabDeleteAllTasks.shrink();
        fabMenu = findViewById(R.id.FAB_menu);
        fabMenu.shrink();
        fabAddTask.setOnClickListener(view -> {
            if (fabAddTask.isExtended()){
                Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
                closeFabMenu();
            } else {
                fabAddTask.extend();
            }

        });
        fabDeleteAllTasks.setOnClickListener(view -> {
            if (fabDeleteAllTasks.isExtended()){
                deleteAllTasksDialog();
                closeFabMenu();
            } else {
                fabDeleteAllTasks.extend();
            }
        });
        fabMenu.setOnClickListener(view -> {
            if (fabMenu.isExtended()){
                closeFabMenu();
            } else {
                openFabMenu();
            }
        });

        recyclerView = findViewById(R.id.recyclerview_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TaskAdapter adapter = new TaskAdapter(this, this);
        recyclerView.setAdapter(adapter);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getTasks().observe(this, adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                delete(adapter.getTaskAt(viewHolder.getAdapterPosition()));
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void openFabMenu() {
        fabAddTask.animate().translationY(-getResources().getDimension(R.dimen.standard_add_task));
        fabDeleteAllTasks.animate().translationY(-getResources().getDimension(R.dimen.standard_delete_all_task)).withEndAction(() -> {
            fabMenu.extend();
            fabDeleteAllTasks.extend();
            fabAddTask.extend();
        });

    }

    private void closeFabMenu() {
        fabMenu.shrink();
        fabAddTask.shrink();
        fabDeleteAllTasks.shrink(new ExtendedFloatingActionButton.OnChangedCallback() {
            @Override
            public void onShrunken(ExtendedFloatingActionButton extendedFab) {
                super.onShrunken(extendedFab);
                fabAddTask.animate().translationY(0);
                fabDeleteAllTasks.animate().translationY(0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK){
            String name = data.getStringExtra(AddEditTaskActivity.EXTRA_NAME);
            String description = data.getStringExtra(AddEditTaskActivity.EXTRA_DESCRIPTION);

            Task task = new Task(name, description, false);
            taskViewModel.insert(task);

            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();

        }  else if (requestCode == EDIT_TASK_REQUEST && resultCode == RESULT_OK){
            int id = data.getIntExtra(AddEditTaskActivity.EXTRA_ID, -1);

            if (id == -1){
                Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = data.getStringExtra(AddEditTaskActivity.EXTRA_NAME);
            String description = data.getStringExtra(AddEditTaskActivity.EXTRA_DESCRIPTION);
            boolean check = data.getBooleanExtra(AddEditTaskActivity.EXTRA_CHECK, false);

            Task task = new Task(name, description, check);
            task.setId(id);
            Log.i(MainActivity.class.getName(), task.toString());

            taskViewModel.update(task);

            Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Task not saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void editTask(Task task) {
        Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);

        intent.putExtra(AddEditTaskActivity.EXTRA_ID, task.getId());
        intent.putExtra(AddEditTaskActivity.EXTRA_NAME, task.getName());
        intent.putExtra(AddEditTaskActivity.EXTRA_DESCRIPTION, task.getDescription());
        intent.putExtra(AddEditTaskActivity.EXTRA_CHECK, task.isCheck());

        startActivityForResult(intent, EDIT_TASK_REQUEST);
    }

    @Override
    public void updateCheck(Task task) {
        taskViewModel.updateCheck(task);
        Log.i(MainActivity.class.getName(), "Updating task " + task.getId() + task.getName() + " to check --- " + task.isCheck());
    }

    @Override
    public void deleteTask(Task task) {
        taskViewModel.delete(task);
        Toast.makeText(MainActivity.this,
                "Task deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteAllTasks() {
        taskViewModel.deleteAllTasks();
    }

    public void deleteAllTasksDialog() {
        DeleteAllTasksDialog deleteAllTasksDialog = new DeleteAllTasksDialog(this);
        deleteAllTasksDialog.show(getSupportFragmentManager(), "delete_all");
    }

    @Override
    public void delete(Task task) {
        DeleteTaskDialog deleteTaskDialog = new DeleteTaskDialog(this, task);
        deleteTaskDialog.show(getSupportFragmentManager(), "delete_task");
    }
}