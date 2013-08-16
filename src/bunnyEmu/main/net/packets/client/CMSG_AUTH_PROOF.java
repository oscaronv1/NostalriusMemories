package bunnyEmu.main.net.packets.client;

import bunnyEmu.main.entities.packet.ClientPacket;
import bunnyEmu.main.utils.BigNumber;
import bunnyEmu.main.utils.BitPack;
import bunnyEmu.main.utils.Log;

/**
 * Authentication proof
 * 
 * @author Marijn
 * 
 */
public class CMSG_AUTH_PROOF extends ClientPacket {

	private String accountName = null;
	private byte[] mClientSeed = new byte[4];
	private byte[] digest = new byte[20];

	// Unused
	private byte[] mClientBuild = new byte[2];
	

	public boolean readVanilla() {
		return readBC();
	}

	public boolean readBC() {
		getInt();// mClientBuild
		getInt(); // unk2
		accountName = getString();
		get(mClientSeed);
		get(digest);
		return true;
	}

	public boolean readWotLK() {
		getInt();// mClientBuild
		getInt(); // unk2
		accountName = getString(); // accountName
		getInt(); // unk3
		get(mClientSeed); // mClientSeed
		getLong();
		getInt();
		getInt();
		getInt();
		get(digest);
		return true;
	}

	public boolean readCata() {
		int position = 0;
		get(digest, position, 7);
		get(new byte[4]);
		get(digest, position += 7, 1);
		get(new byte[12]);
		get(digest, position += 1, 1);
		get(new byte[1]);
		get(digest, position += 1, 2);
		get(mClientSeed);

		get(new byte[4]);
		get(digest, position += 2, 6);
		get(mClientBuild);// mClientBuild
		Log.log(new BigNumber(mClientBuild).toHexString());
		get(digest, position += 6, 1);
		get(new byte[5]);

		get(digest, position += 1, 2);

		int firstByte = (0x000000FF & ((int) get()));
		short addonSize = (short) firstByte;

		get(new byte[addonSize + 3]); // adjusting to addonsize (1+3 = int)
		accountName = getString();
		return true;
	}

	public boolean readMoP() {
		get(new byte[54]);
		int addonSize = getInt();
		get(new byte[addonSize + 2]); // +2 = 0-byte and "X"?
		accountName = getString();
		
		BitPack bitPack = new BitPack(this);
		
		bitPack.write(0); // inqueue
		bitPack.write(1); // account data
		
		bitPack.write(0);
		bitPack.write(0);
		bitPack.write(13, 23);	// race count
		bitPack.write(0);
		bitPack.write(0, 21);
		
		bitPack.write(11, 23);	// class count
		bitPack.write(0, 22);
		bitPack.write(0);
		
		bitPack.flush();
		
		this.put((byte) 0);
		
		for (int c = 0; c < 11; c++) {	// activate classes
			this.put((byte) 0);
			this.put((byte) (c+1));
		}
		
		for (int c = 0; c < 13; c++) {	// activate races
			this.put((byte) 0);
			this.put((byte) (c+1));
		}
		
		this.putInt(0);
		this.putInt(0);
		this.putInt(0);
		
		this.put((byte) 3);	// expansion
		this.put((byte) 3);	// expansion
		
		
		return true;
	}

	public String getAccountName() {
		return accountName;
	}
	
	public byte[] getClientSeed() {
		return mClientSeed;
	}
	
	public byte[] getDigest() {
		return digest;
	}

}