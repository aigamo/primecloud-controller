/*
 * Copyright 2014 by SCSK Corporation.
 * 
 * This file is part of PrimeCloud Controller(TM).
 * 
 * PrimeCloud Controller(TM) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PrimeCloud Controller(TM) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PrimeCloud Controller(TM). If not, see <http://www.gnu.org/licenses/>.
 */
package jp.primecloud.auto.tool.management.iaasgw;

import jp.primecloud.auto.iaasgw.IaasGatewayWrapper;

public class IaasGatewayFactory {

    protected Integer describeInterval;

    protected boolean sync = false;

    public IaasGatewayFactory(Integer describeInterval, boolean sync) {
        this.describeInterval = describeInterval;
        this.sync = sync;
    }

    public IaasGatewayWrapper createIaasGateway(Long userNo, Long platformNo) {
        return new IaasGatewayWrapper(userNo, platformNo, describeInterval, null);
    }

    /**
     * describeIntervalを設定します。
     *
     * @param describeInterval describeInterval
     */
    public void setDescribeInterval(Integer describeInterval) {
        this.describeInterval = describeInterval;
    }

    /**
     * syncを設定します。
     *
     * @param sync sync
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
