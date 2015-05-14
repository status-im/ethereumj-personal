package org.ethereum.ethereum_android;

import org.ethereum.core.AccountState;
import org.ethereum.core.Denomination;
import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 * Created by Adrian Tiberius on 06.05.2015.
 */
public class AccountsDataAdapter {

    List<DataClass> data;

    final String[] columns = new String[]{"Account", "Balance", "Is Contract"};

    public AccountsDataAdapter(List<DataClass> data) {

        this.data = data;
    }

    public void addDataPiece(DataClass d) {

        data.add(d);
        //this.fireTableRowsInserted(Math.min(data.size() - 2, 0), data.size() - 1);
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public boolean isCellEditable(int row, int column) { // custom isCellEditable function
        return column == 0 ? true : false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return Hex.toHexString(data.get(rowIndex).address);
        } else if (columnIndex == 1) {
            if (data.get(rowIndex).accountState != null) {
                return Denomination.toFriendlyString(data.get(rowIndex).accountState.getBalance());
            }
            return "---";
        } else {
            if (data.get(rowIndex).accountState != null) {
                if (!Arrays.areEqual(data.get(rowIndex).accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH))
                    return "Yes";
            }
            return "No";
        }
    }

    public static class DataClass {
        public byte[] address;
        public AccountState accountState;
    }
}
