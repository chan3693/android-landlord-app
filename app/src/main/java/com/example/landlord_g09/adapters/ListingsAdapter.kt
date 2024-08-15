package com.example.landlord_g09.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.landlord_g09.R
import com.example.landlord_g09.models.Property

class ListingsAdapter(
    var yourListData:List<Property>,
    var functionOnClick: (Int)->Unit
)
    : RecyclerView.Adapter<ListingsAdapter.PropertyViewHolder>(){

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) {
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_layout, parent, false)

        return PropertyViewHolder(view)
    }


    override fun getItemCount(): Int {
        return yourListData.size
    }


    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property:Property = yourListData.get(position)

        val tvImageView = holder.itemView.findViewById<ImageView>(R.id.imageView)
        Glide.with(holder.itemView.context)
            .load(property.imageUrl)
            .into(tvImageView)

        val tvAddress = holder.itemView.findViewById<TextView>(R.id.tvPropertyAddress)
        tvAddress.text = property.address

        val tvPrice = holder.itemView.findViewById<TextView>(R.id.tvPrice)
        tvPrice.text = "Price: $${
            if (property.monthlyRentalPrice % 1.0 == 0.0) property.monthlyRentalPrice.toInt() 
            else property.monthlyRentalPrice}/month"

        val tvIsAvailable = holder.itemView.findViewById<TextView>(R.id.tvIsAvailable)
        if (property.isAvailable == true){
            tvIsAvailable.text = "Available"
        } else {
            tvIsAvailable.text = "Not Available"
        }

        val onClick = holder.itemView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.btnToPropertyDetails)
        onClick.setOnClickListener{
            functionOnClick(position)
        }
    }
}