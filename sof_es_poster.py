#!/usr/bin/python

from pprint import pprint
from pyes import *
import csv
import string

# for the sample we want to clear the index before indexing again
conn = ES('http://127.0.0.1:9200')
try:
  conn.indices.delete_index("sof-sample")
except:
  pass

conn.indices.create_index("sof-sample")

# create document mapping
mapping = {
    'post_id': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'post_creation_date': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'owner_user_id': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'owner_creation_date': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'reputation_at_post_creation': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'owner_undeleted_answer_count_at_post_time': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'title': {
	'boost': 1.0,
	'index': 'analyzed',
	'store': 'yes',
	'type': 'string'},
    'body': {
	'boost': 1.0,
	'index': 'analyzed',
	'store': 'yes',
	'type': 'string'},
    'tag_1': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'tag_2': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'tag_3': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'tag_4': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'tag_5': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'post_closed_date': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'},
    'open_status': {
	'boost': 1.0,
	'index': 'not_analyzed',
	'store': 'yes',
	'type': 'string'}}
conn.indices.put_mapping("sof-document", {'properties':mapping}, ["sof-sample"])

# index documents
counter = 1
with open('../train-sample', 'rt') as csvfile:
  sofreader = csv.reader(csvfile, delimiter=',', quotechar='"')
  for row in sofreader:
    [post_id, post_creation_date, owner_user_id, owner_creation_date, reputation_at_post_creation, owner_undeleted_answer_count_at_post_time, title, body, tag_1, tag_2, tag_3, tag_4, tag_5, post_closed_date, open_status] = row;
    document = {
        "post_id": post_id,
        "post_creation_date": post_creation_date,
        "owner_user_id": owner_user_id,
        "owner_creation_date" : owner_creation_date,
        "reputation_at_post_creation": reputation_at_post_creation,
        "owner_undeleted_answer_count_at_post_time": owner_undeleted_answer_count_at_post_time,
        "title": title,
        "body": body,
        "tag_1": tag_1,
        "tag_2": tag_2,
        "tag_3": tag_3,
        "tag_4": tag_4,
        "tag_5": tag_5,
        "post_closed_date": post_closed_date,
        "open_status": open_status}
    print "Indexing"
    print counter
    print post_id
    conn.index(document, "sof-sample", "sof-document", counter)
    counter = counter + 1

conn.indices.refresh(sof-sample)

q = TermQuery("title", "Elastic Search")
results = conn.search(query = q)
for r in results:
  pprint(r)
