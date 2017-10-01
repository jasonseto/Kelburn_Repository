val user_data = sc.textFile("hdfs:///data/ml-100k/u.user")
user_data.first

val user_fields = user_data.map(line => line.split("\\|"))
user_fields.take(1)
val num_users = user_fields.map(fields => fields(0)).count
val num_genders = user_fields.map(fields => fields(2)).distinct.count
val num_occupations = user_fields.map(fields => fields(3)).distinct.count
val num_zipcodes = user_fields.map(fields => fields(4)).distinct.count
println("Users: %d, genders: %d, occupations: %d, ZIP codes: %d".format(num_users, num_genders, num_occupations, num_zipcodes))
