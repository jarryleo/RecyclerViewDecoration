package cn.leo.recyclerviewdecoration;

/**
 * Created by Leo on 2018/6/8.
 */

public class DataPacket {
    /**
     * 数据头
     */
    public static final int HEAD = 0xA00A;
    /**
     * 单台秤指令
     */
    //校验指令
    public static final byte BALANCE_CHECK = 0x01;
    //时间校准指令
    public static final byte BALANCE_TIME_CALIBRATION = 0x02;
    //获取重量指令
    public static final byte BALANCE_GET_WEIGHT = 0x03;
    //收发频率指令
    public static final byte BALANCE_RECEIVE_SEND_FREQUENCY = 0x04;
    //重量校准指令
    public static final byte BALANCE_WEIGHT_CALIBRATION = 0x05;
    //模式切换指令（单位之间切换）
    public static final byte BALANCE_UNIT_SWITCH = 0x06;
    //LED控制指令
    public static final byte BALANCE_LED_CONTROL = 0x07;
    /**
     * 网关指令
     */
    //获取重量指令
    public static final byte GATEWAY_GET_WEIGHT = 0x08;
    //LED状态控制
    public static final byte GATEWAY_LED_STATUS_CONTROL = 0x09;
    //获取所有LED灯的状态
    public static final byte GATEWAY_GET_ALL_LED_STATUS = 0x10;

    //数据包数据
    private byte[] mData;
    //地址byte数组
    private byte[] mMacAddress;
    //解码后的指令码
    private byte mCode;
    //解码后的参数
    private byte[] mParams;

    public DataPacket() {
    }

    public DataPacket(byte[] data) {
        this.mData = data;
        decodeData();
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        this.mData = data;
        //解码
        decodeData();
    }

    /**
     * 获取 解码后的  mac 地址
     *
     * @return mac 地址 byte
     */
    public byte[] getMacAddress() {
        return mMacAddress;
    }

    /***
     *  获取解码后的 指令
     * @return 指令
     */
    public byte getCode() {
        return mCode;
    }

    /**
     * 获取解码 后的参数值
     *
     * @return 参数
     */
    public byte[] getParams() {
        return mParams;
    }

    /**
     * 获取数据包校验结果
     *
     * @return 数据包完整性
     */
    public boolean getCheckResult() {
        if (mData == null) return false;
        if (mData.length < 4) return false;
        byte lengthCode = mData[3];
        if (lengthCode != mData.length) return false;
        byte checkCode = mData[2];
        return (checkCode + lengthCode) % 256 == 0;
    }

    /**
     * 构建数据包
     *
     * @param macAddress mac地址 F8-BC-12-A0-0C-AA
     * @param code       指令
     * @param params     参数
     * @return 数据包对象 获取数据值用 {@link DataPacket#getData()}
     */
    public DataPacket createPacket(String macAddress, byte code, byte[] params) {
        byte[] bytes = stringMacToBytes(macAddress);
        createPacket(bytes, code, params);
        return this;
    }


    /**
     * 字符串MAC转成byte数组
     *
     * @param macAddress 字符串MAC地址
     * @return mac byte数组
     */
    public byte[] stringMacToBytes(String macAddress) {
        if (macAddress == null || macAddress.length() != 17) return null;
        String splitWord = macAddress.substring(2, 3);
        String[] split = macAddress.split(splitWord);
        if (split.length != 6) return null;
        byte[] mac = new byte[6];
        for (int i = 0; i < 6; i++) {
            try {
                mac[i] = (byte) Integer.parseInt(split[i], 16);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return mac;
    }

    /**
     * 构建数据包
     *
     * @param macAddress mac地址
     * @param code       指令
     * @param params     参数
     * @return 数据包对象 获取数据值用 {@link DataPacket#getData()}
     */
    public DataPacket createPacket(byte[] macAddress, byte code, byte[] params) {
        if (macAddress == null) return null;
        int packetLength = 4 + macAddress.length + 1 + params.length;
        mData = new byte[packetLength];
        //组装头部
        byte[] head = getIntByte(HEAD, 2);
        System.arraycopy(head, 0, mData, 0, 2);
        //组装校验码
        byte checkCode = (byte) (256 - packetLength);
        mData[2] = checkCode;
        //组装长度值
        mData[3] = (byte) packetLength;
        //组装地址
        System.arraycopy(macAddress, 0, mData, 4, 6);
        //组装指令
        mData[10] = code;
        //组装参数
        System.arraycopy(params, 0, mData, 11, params.length);
        //组装完毕，解码获取参数
        mMacAddress = macAddress;
        mCode = code;
        mParams = params;
        return this;
    }

    /**
     * @param intNum 要获取byte的int值
     * @param count  一个int 4 个 byte 要取几个 最多4个
     * @return 从 int 里面截取的 byte
     */
    public byte[] getIntByte(int intNum, int count) {
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = (byte) (intNum >>> (count - i - 1) * 8);
        }
        return bytes;
    }

    /**
     * 解码数据包
     *
     * @return 返回本对象
     */
    public DataPacket decodeData() {
        if (!getCheckResult()) return this;
        //获取mac地址
        mMacAddress = new byte[6];
        System.arraycopy(mData, 4, mMacAddress, 0, 6);
        //获取指令码
        mCode = mData[10];
        //获取参数
        int paramLength = mData.length - 11;
        mParams = new byte[paramLength];
        System.arraycopy(mData, 10, mParams, 0, paramLength);
        return this;
    }
}
