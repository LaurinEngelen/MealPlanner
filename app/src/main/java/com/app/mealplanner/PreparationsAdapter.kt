import android.text.Html
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.R

class PreparationsAdapter(
    private val preparations: MutableList<String>,
    private val textColorResId: Int,
    private val onDelete: (Int) -> Unit = {},
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit = {},
    private val showEditIcons: Boolean = true
) : RecyclerView.Adapter<PreparationsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.preparationText)
        val deleteButton: ImageView = itemView.findViewById(R.id.deletePreparation)
        val dragHandle: ImageView = itemView.findViewById(R.id.dragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preparation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preparation = preparations[position]
        holder.textView.text = Html.fromHtml("<b>${position + 1}.</b> $preparation", Html.FROM_HTML_MODE_LEGACY)
        holder.textView.setTextColor(
            ContextCompat.getColor(holder.itemView.context, textColorResId)
        )
        if (showEditIcons) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.dragHandle.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDelete(holder.adapterPosition)
            }
            holder.dragHandle.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onStartDrag(holder)
                }
                false
            }
        } else {
            holder.deleteButton.visibility = View.GONE
            holder.dragHandle.visibility = View.GONE
            holder.deleteButton.setOnClickListener(null)
            holder.dragHandle.setOnTouchListener(null)
        }
    }

    override fun getItemCount(): Int = preparations.size

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val item = preparations.removeAt(fromPosition)
        preparations.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        notifyDataSetChanged() // Nummerierung nach Verschieben aktualisieren
    }

    fun onItemDismiss(position: Int) {
        preparations.removeAt(position)
        notifyItemRemoved(position)
    }
}