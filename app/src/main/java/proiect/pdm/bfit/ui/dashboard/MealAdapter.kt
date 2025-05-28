package proiect.pdm.bfit.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.MealEntity

class MealAdapter(
    private var meals: List<MealEntity>,
    private val onDeleteClick: (MealEntity) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mealNameText: TextView = view.findViewById(R.id.mealNameText)
        val mealQuantityText: TextView = view.findViewById(R.id.mealQuantityText)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteMealButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealNameText.text = meal.name
        holder.mealQuantityText.text = "${meal.quantity} ${meal.unit}"
        holder.deleteButton.setOnClickListener { onDeleteClick(meal) }
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<MealEntity>) {
        meals = newMeals
        notifyDataSetChanged()
    }
} 