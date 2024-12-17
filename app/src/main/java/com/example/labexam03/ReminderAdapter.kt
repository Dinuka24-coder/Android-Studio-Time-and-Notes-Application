import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lab03ex.R
import com.example.labexam03.ReminderTask

class ReminderAdapter(
    private val context: Context,
    private var reminders: MutableList<ReminderTask>,
    private val onEdit: (ReminderTask) -> Unit,
    private val onDelete: (ReminderTask) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
        val buttonEdit: Button = view.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.textViewDescription.text = reminder.description
        holder.buttonEdit.setOnClickListener { onEdit(reminder) }
        holder.buttonDelete.setOnClickListener { onDelete(reminder) }
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<ReminderTask>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }
}
