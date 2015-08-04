package enigma_edit.error;

import enigma_edit.Resources;

public class PositionOutOfBounds extends GeneralError
{
	private static final long serialVersionUID = 1L;
	
	public PositionOutOfBounds(String id, int posX, int posY)
	{
		super(String.format(Resources.errors.getString("PositionOutOfBounds"), Integer.toString(posX), Integer.toString(posY)));
	}
}
