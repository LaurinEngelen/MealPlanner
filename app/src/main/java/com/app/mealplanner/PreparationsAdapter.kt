import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.mealplanner.R

class PreparationsAdapter(private val preparations: List<String>) :
    RecyclerView.Adapter<PreparationsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.ingredientText) // Reuse the same layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false) // Reuse the same layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preparation = preparations[position]
        holder.textView.text = "${position + 1}. $preparation" // Add step number
        holder.textView.setTextColor(
            ContextCompat.getColor(holder.itemView.context, android.R.color.white)
        )
    }

    override fun getItemCount(): Int = preparations.size
}