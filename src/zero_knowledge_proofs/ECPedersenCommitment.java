package zero_knowledge_proofs;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Base64;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import zero_knowledge_proofs.CryptoData.CryptoData;

public class ECPedersenCommitment extends PedersenCommitment implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4464353885259184169L;
	protected byte[] data;
	
	public static final ECPedersenCommitment ZERO = new ECPedersenCommitment();
	
	private ECPedersenCommitment()
	{
		data = new byte[] {0};
	}
	
	public ECPedersenCommitment(BigInteger message, BigInteger keys, CryptoData environment)
	{
		//g^m h^r 
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		ECPoint g = e[0].getECPointData(c);
		ECPoint h = e[1].getECPointData(c);
		ECPoint comm = g.multiply(message).add(h.multiply(keys));
		data = comm.getEncoded(true);
	}
	private ECPedersenCommitment(ECPoint comm)
	{
		data = comm.getEncoded(true);
	}

	public ECPoint getCommitment(CryptoData environment) {
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		return c.decodePoint(data);
	}

	public boolean verifyCommitment(BigInteger message, BigInteger keys, CryptoData environment) {

		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		ECPoint g = e[0].getECPointData(c);
		ECPoint h = e[1].getECPointData(c);
		ECPoint comm = g.multiply(message).add(h.multiply(keys));
		return getCommitment(environment).equals(comm);
	}

	public PedersenCommitment multiplyCommitment(PedersenCommitment _cmt, CryptoData environment) {

		ECPedersenCommitment cmt = (ECPedersenCommitment) _cmt;
		return new ECPedersenCommitment(cmt.getCommitment(environment).add(getCommitment(environment)));
	}
	public ECPedersenCommitment multiplyShiftedCommitment(PedersenCommitment _cmt, int lShift, CryptoData environment) {

		ECPedersenCommitment cmt = (ECPedersenCommitment) _cmt;
		return new ECPedersenCommitment((cmt.getCommitment(environment).multiply(BigInteger.ONE.shiftLeft(lShift))).add(getCommitment(environment)));
	}
	
	public String toString64()
	{
		return String.format("(%s)", Base64.getEncoder().encodeToString(data));
	}

	public String toString()
	{
		return String.format("(%s)", new BigInteger(data));
	}
	public static ECPedersenCommitment product(ECPedersenCommitment[] comms, CryptoData environment)
	{
		CryptoData[] e = environment.getCryptoDataArray();
		ECCurve c = e[0].getECCurveData();
		ECPoint point = c.getInfinity();
		for(int i = 0; i < comms.length; i++)
		{
			point = point.add(comms[i].getCommitment(environment));
		}
		return new ECPedersenCommitment(point);
	}
	
}
