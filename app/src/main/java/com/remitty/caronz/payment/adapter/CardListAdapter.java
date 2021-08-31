package com.remitty.caronz.payment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.CreditCard;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.OrderViewHolder> {
    private List<CreditCard> arrItems;
    private Listener listener;

    public CardListAdapter(List<CreditCard> arrItems, boolean viewType) {
        this.arrItems = arrItems;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvCardNumber;
        Button btnCardDelete;

        public OrderViewHolder(View view) {
            super(view);

            tvCardNumber = view.findViewById(R.id.card_number);
            btnCardDelete = view.findViewById(R.id.btn_card_delete);
        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        OrderViewHolder vh = new OrderViewHolder(mView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderViewHolder holder, final int position) {
        CreditCard item = arrItems.get(position);

        holder.tvCardNumber.setText(item.getCardNo());

        holder.btnCardDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnDelete(position);
            }
        });

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return arrItems.size();
    }

    public Object getItem(int i) {
        return arrItems.get(i);
    }

    public interface Listener {
        /**
         * @param position
         */
        void OnDelete(int position);
    }
}
