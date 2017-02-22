import pandas as pd
import sys
import numpy as np


def begin():
    f = open("fully_sorted.csv")
    new_name = f.name[0:f.name.index(".")] + "_final.hdf5"
    store = pd.HDFStore(new_name, mode="a")
    titles = ["vendor_id", "pickup_datetime", "dropoff_datetime", "passenger_count",
              "trip_distance", "pickup_longitude", "pickup_latitude", "rate_code", "store_and_fwd_flag",
              "dropoff_longitude", "dropoff_latitude", "payment_type", "fare_amount", "surcharge", "mta_tax",
              "tip_amount", "tolls_amount", "total_amount"]
    types = {t: np.float32 for t in titles}
    types.update({titles[0]: str, titles[1]: str, titles[2]: str, titles[8]: str, titles[11]: str})
    for chunk in pd.read_csv(f, chunksize=1000000, names=titles, dtype=types, skiprows=1):
        print "Reading chunk"
        store.append("all_taxi_data", chunk, min_itemsize={t: 22 for t in titles[1:2]})
    f.close()
    store.close()

if __name__ == "__main__":
    begin()
