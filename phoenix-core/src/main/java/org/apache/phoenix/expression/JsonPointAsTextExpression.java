package org.apache.phoenix.expression;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.visitor.ExpressionVisitor;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PJson;
import org.apache.phoenix.schema.types.PVarchar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPointAsTextExpression extends BaseCompoundExpression{

	private static final Logger logger = LoggerFactory.getLogger(JsonPointAsTextExpression.class);
	
	public JsonPointAsTextExpression(List<Expression> children)
	{
		super(children);
	}
	public JsonPointAsTextExpression()
	{
		
	}
	@Override
	public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr) {
		if (!children.get(0).evaluate(tuple, ptr)) {
            if (logger.isDebugEnabled()) {
                logger.debug("->> left value is null");
            }
            return false;
        }
		String source = (String) PJson.INSTANCE.toObject(ptr, children.get(0).getSortOrder());
		if (!children.get(1).evaluate(tuple, ptr)) {
            if (logger.isDebugEnabled()) {
                logger.debug("->> right value is null");
            }
            return false;
        }
		String key = (String) PVarchar.INSTANCE.toObject(ptr, children.get(1).getSortOrder());
		try {
		ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonTree=mapper.readTree(source);
				if(jsonTree.has(key))
				{
					if(jsonTree.get(key).isValueNode())
						{
							ptr.set(jsonTree.get(key).asText().getBytes());
							return true;
						}
					else
					{
						ptr.set(jsonTree.get(key).toString().getBytes());
						return true;
					}
				}
				else
				{
					return false;
				}
		} catch (JsonParseException e) {
			e.printStackTrace();
			return false;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	 @Override
	    public final <T> T accept(ExpressionVisitor<T> visitor) {
	        List<T> l = acceptChildren(visitor, visitor.visitEnter(this));
	        T t = visitor.visitLeave(this, l);
	        if (t == null) {
	            t = visitor.defaultReturn(this, l);
	        }
	        return t;
	    }
	    @Override
	    public void readFields(DataInput input) throws IOException {
	        super.readFields(input);
	    }

	    @Override
	    public void write(DataOutput output) throws IOException {
	        super.write(output);
	    }
	    
		@Override
		public PDataType getDataType() {
			return PVarchar.INSTANCE;
		}
}
