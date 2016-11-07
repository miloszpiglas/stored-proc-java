create type ctype
(
	str varchar(10),
	num integer
);

-- procedure accepts single input argument and returns set of tuples
create or replace function gen_rows(nrows integer) returns setof ctype as $$
	declare
		str varchar(10); nr integer;
	begin
		nr := 0;
		while nr < nrows loop
			str := 'ROW'||nr; nr := nr + 1;
			return next (str, nr);
		end loop;
		return;
	end;
$$ language plpgsql;

-- procedure accepts two input arguments and returns single numeric value
create or replace function num_sum(anum integer, bnum integer) returns integer as $$
begin
	return anum + bnum;
end;
$$ language plpgsql;

-- procedure puts string to ouput argument
create or replace function out_text(out txt varchar(10)) as $$
begin
	txt := 'out_text_result';
end;
$$ language plpgsql;

-- procedure accepts two numeric values and sets results to output arguments
create or replace function modmul(anum integer, bnum integer, out result integer, out modulo integer) as $$
begin
	 result := anum / bnum;
	 modulo := anum % bnum;
end;
$$ language plpgsql;

-- procedure generates set of integers
create or replace function int_set(len integer) returns setof integer as $$
begin
	for i in 1..len loop
		return next i * 100;
	end loop;
end;
$$ language plpgsql;

