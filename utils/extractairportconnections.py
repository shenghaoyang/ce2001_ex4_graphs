#!/usr/bin/env python3.7

import csv
import argparse
import pathlib
import sys


def main(args):
    parser = argparse.ArgumentParser(
        description=('Process a openflights.org route database in CSV format '
                     'and produce a graph in CSV format with airports as nodes '
                     'and edges between airports representing the presence of '
                     'direct (non-stop) bidirectional path(s) between them. '
                     'By default, data is written to standard output. '
                     'By default, UTF-8 is used for text encoding / decoding.'))
    parser.add_argument('INPUT', nargs='?',
                        default=sys.stdin,
                        type=argparse.FileType('r', encoding='UTF-8'),
                        help='path to route database in CSV format '
                             '(defaults to stdin)')
    parser.add_argument('OUTPUT', nargs='?',
                        default=sys.stdout,
                        type=argparse.FileType('w', encoding='UTF-8'),
                        help='path to output file (defaults to stdout)')
    arguments = parser.parse_args(args[1:])
    
    fieldnames = ('Airline', 'Airline ID', 'Source', 'Source ID',
                  'Destination', 'Destination ID', 'Codeshare',
                  'Stops', 'Equipment')
                  
    airports = dict()
    reader = csv.DictReader(arguments.INPUT, fieldnames=fieldnames)
    writer = csv.writer(arguments.OUTPUT, csv.unix_dialect)
    
    for row in reader:
        # Since we are only concerned about airports that have direct 
        # connections, don't use routes between two airports that have stops 
        # to infer that there is a direct connection between those two airports.
        if int(row['Stops']):
            continue
    
        s_airport = row['Source']
        d_airport = row['Destination']
        
        # Add the link from one airport to the other.
        airports.setdefault(s_airport, set()).add(d_airport)
        
    arguments.INPUT.close()
     
    # We should be done with the links by now, but we are not.
    # The data in the routes database is _directional_, meaning that
    # a direct link from airport A to B does not imply the existence of a 
    # similar route from airport B to A. We need to remove unidirectional
    # links since we only want to write out a graph containing bidirectional
    # ones. 
    for airport, connected_airports in airports.items():

        unidirectional_airports = set()

        for connected_airport in connected_airports:
            # Short-circuit if the connected airport doesn't exist in the
            # airport to avoid checking whether the airport exists in the
            # connected airports set of the connected airport.
            if ((connected_airport in airports) and 
                (airport in airports[connected_airport])):
                continue
            
            unidirectional_airports.add(connected_airport)

        connected_airports -= unidirectional_airports

    # Now, we write out the CSV formatted data representing the airport
    # connection graph.
    #
    # CSV output formatting uses the unix format (complaint to the CSV RFC).
    # The first column of each row contains the IATA airport code of an airport, 
    # and the latter columns contain the airport IATA codes of airports that are
    # connected via a direct flight to the airport indicated in the first
    # column.
    for airport, connected_airports in airports.items():
        writer.writerow((airport, *connected_airports))

    arguments.OUTPUT.close()      


if __name__ == '__main__':
    main(sys.argv)
