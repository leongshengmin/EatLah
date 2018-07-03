package com.eatlah.eatlah.listeners;

import android.app.Activity;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.eatlah.eatlah.activities.Customer.CustomerHomepage;
import com.eatlah.eatlah.adapters.Customer.OrderRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

/**
 * Detects left and right swipes across a view.
 */
public class OnSwipeTouchListener implements OnTouchListener {

    private final Activity mContext;
    private final OrderItem orderItem;
    private final OrderRecyclerViewAdapter mAdapter;
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context, OrderRecyclerViewAdapter mAdapter, OrderItem orderItem) {
        this.orderItem = orderItem;
        this.mAdapter = mAdapter;
        this.mContext = (Activity) context;
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    // remove item from cart
    public void onSwipeLeft() {
        System.out.println("Left swipe detected");
        System.out.println("context: " + mContext);
        Order order = ((CustomerHomepage) mContext).getOrder();
        System.out.println("order from customer homepage contains : " + order.getOrders());
        order.removeOrder(orderItem, mAdapter);
    }

    // add a single foodItem of orderItem to cart
    public void onSwipeRight() {
        System.out.println("right swipe detected");
        ((CustomerHomepage) mContext).getOrder().addOrder(orderItem, mAdapter);
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }
    }
}