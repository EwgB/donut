# donut
Test project for atrify

This application implements a service for adding, storing, reading and deleting donut orders, as well as preparing a donut order for delivery. Following endpoints are available:

* `GET /orders`: Returns all order in their priority, together with their position in the queue and estimated wait time.
* `POST /orders`: Adds a new order to the queue. Orders from premium customers are added before all regular orders.
* `GET /orders/{id}`: Returns a single order by its ID.
* `GET /orders?clientId={id}`: Returns a single order by the client ID.
* `DELETE /orders`: Deletes an order for a particular client.
* `GET /nextDelivery`: Returns a list of orders for the next delivery. Subsequent calls without calling `DELETE /nextDelivery` return the same list.
* `DELETE /nextDelivery`: Finishes a delivery by deleting the orders from a previous `GET /nextDelivery` from the database.
